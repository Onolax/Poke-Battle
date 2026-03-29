# Phase 2 — Game Services Design Spec

**Date:** 2026-03-29
**Status:** Approved

---

## Goal

Implement the three remaining stub services — lobby-service, battle-service, rating-service — to make Poke-Battle fully playable. Add Kafka (KRaft mode) to infra as the async event bus connecting all three services.

---

## Scope

- Kafka added to `infra/docker-compose.yml` and `infra/k8s/`
- `lobby-service`: matchmaking queue (Redis Sorted Set) + polling endpoint + Kafka producer
- `battle-service`: WebSocket STOMP + full Gen 9 turn engine + Redis state + Kafka consumer/producer
- `rating-service`: Elo calculation + Flyway schema + Kafka consumer + leaderboard endpoint

**Out of scope:** turn timer, spectators, replays, notifications, OAuth2 social login, multiple formats/generations.

---

## Kafka Infrastructure

### Docker Compose addition

```yaml
kafka:
  image: apache/kafka:3.7.0
  ports:
    - "9092:9092"
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
```

### K8s addition (`infra/k8s/kafka.yml`)

Single-pod Deployment + ClusterIP Service on port 9092.

### Topics (auto-created)

| Topic | Producer | Consumer | Payload |
|-------|----------|----------|---------|
| `match.found` | lobby-service | battle-service | `{battleId, player1Id, player1TeamId, player2Id, player2TeamId, format}` |
| `battle.started` | battle-service | _(unused Phase 2)_ | `{battleId, player1Id, player2Id}` |
| `battle.ended` | battle-service | rating-service | `{battleId, winnerId, winnerUsername, loserId, loserUsername, format, turns}` |

### pom.xml addition (lobby, battle, rating)

```xml
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>
```

---

## lobby-service

### Package structure

```
com.pokemon.lobby
├── controller/     LobbyController
├── service/        LobbyService, MatchmakerScheduler
├── kafka/          MatchFoundProducer
├── dto/            QueueRequest, QueueStatusResponse, MatchFoundEvent
└── config/         RedisConfig, KafkaProducerConfig
```

### API

```
POST   /api/lobby/queue     X-User-Id header, body: {teamId, format}  → 200 {status:"QUEUED"}
GET    /api/lobby/status    X-User-Id header                           → 200 {status:"WAITING"|"MATCHED", battleId?}
DELETE /api/lobby/queue     X-User-Id header                           → 204
```

`X-User-Id` is injected by the api-gateway JWT filter — lobby-service trusts it without re-validation.

### Redis keys

```
queue:{format}                Sorted Set   member="{userId}:{teamId}"   score=epochMillis
lobby:user:{userId}:battleId  String       TTL 10 min
```

### Matchmaker (`@Scheduled` every 1 second)

1. `ZRANGE queue:GEN9OU 0 1 WITHSCORES` — peek at top 2
2. If 2 members exist: execute Lua script atomically:
   - `ZREM queue:GEN9OU member1 member2`
   - Returns both members (parse `userId:teamId`)
3. Generate `battleId` (UUID)
4. `SET lobby:user:{p1Id}:battleId {battleId} EX 600`
5. `SET lobby:user:{p2Id}:battleId {battleId} EX 600`
6. Publish `match.found` Kafka event

### Tests

- `LobbyServiceTest` — unit test queue/dequeue/status logic with mocked `RedisTemplate`
- `MatchmakerSchedulerTest` — verify Lua script invocation and Kafka publish

---

## battle-service

### Package structure

```
com.pokemon.battle
├── controller/     BattleController
├── websocket/      BattleWebSocketHandler, StompConfig, JwtHandshakeInterceptor
├── service/        BattleService, TurnResolutionService
├── engine/         DamageCalculator, StatusEffectProcessor, WinConditionChecker, MoveResolver
├── kafka/          MatchFoundConsumer, BattleEventProducer
├── redis/          BattleStateRepository, BattlePubSubListener
├── client/         GameDataClient, UserServiceClient
├── dto/            BattleState, BattlePokemon, TurnResult, MoveAction,
│                   MatchFoundEvent, BattleEndedEvent
└── config/         WebSocketConfig, RedisConfig, KafkaConfig
```

### REST API

```
GET    /api/battle/{battleId}/state    → current battle state (for reconnect)
POST   /api/battle/{battleId}/forfeit  → forfeit current battle
```

### WebSocket (STOMP)

```
Endpoint:    /ws/battle  (SockJS fallback enabled)
Subscribe:   /topic/battle/{battleId}    ← turn results, battle end events
Send moves:  /app/battle/{battleId}/move    body: {moveSlug}
Send switch: /app/battle/{battleId}/switch  body: {slot}  (0-5)
Forfeit WS:  /app/battle/{battleId}/forfeit
```

JWT validated on handshake via `JwtHandshakeInterceptor` — extracts `userId` into WebSocket session attributes.

### Battle initialisation (Kafka consumer `match.found`)

1. Fetch both teams from user-service directly (bypassing gateway): `GET http://user-service:8081/api/teams/{teamId}` with header `X-Internal-Call: true`. user-service `SecurityConfig` must permit requests carrying this header without JWT validation.
2. Fetch move/pokemon data from game-data-service for stat/type lookup
3. Build `BattlePokemon[]` — set `currentHp = baseHp`, `pp = maxPP`, `status = null`, `statBoosts = {}`
4. Write to Redis:
   - `battle:{id}:state` Hash: `{phase, turnNumber, weather, activeSlot1, activeSlot2, player1Id, player2Id}`
   - `battle:{id}:team:p1` String (JSON)
   - `battle:{id}:team:p2` String (JSON)
   - `battle:{id}:moves:pending` Hash (empty)
   - All keys: TTL 2 hours (reset on each turn)
5. Publish `battle.started` Kafka event

### Turn resolution

```
Player submits move →
  HSETNX battle:{id}:moves:pending  playerN  moveSlug
  HLEN == 2?
    → acquire Redis lock: SET battle:{id}:lock {podId} NX EX 5
    → resolve in priority order, then speed order (ties: random)
    → for each action:
        if SWITCH: swap active slot
        if MOVE:
          calc damage (formula below)
          apply secondary effects (status condition, stat boost)
          deduct PP
          apply end-of-turn status damage (BRN 1/16, PSN 1/8)
          tick status counters (SLP, FRZ thaw)
          check faint → mark fainted, require switch
    → update battle:{id}:state, team:p1, team:p2 in Redis
    → DEL battle:{id}:moves:pending, battle:{id}:lock
    → PUBLISH battle:{id}:events  {type:TURN_RESULT, turnNumber, actions, teamP1, teamP2}
    → check win condition → if over:
        PUBLISH {type:BATTLE_END, winnerId}
        Kafka battle.ended
        SET TTL 5 min on all battle:* keys
```

All pods subscribe to `battle:{id}:events` Pub/Sub and forward messages to locally-connected WebSocket sessions via `/topic/battle/{battleId}`.

### Damage formula (Gen 9)

```
damage = floor(floor((floor(2*level/5 + 2) * power * atk/def) / 50) + 2) * modifier

modifier = stab * typeEffectiveness * critMultiplier * random
  stab              = 1.5 if attacker type matches move type, else 1.0
  typeEffectiveness = product of chart lookups (0, 0.25, 0.5, 1, 2, 4)
  critMultiplier    = 1.5 if random(0,1) < 0.04167 (1/24), else 1.0
  random            = uniform(0.85, 1.0)
level = 50 (all competitive battles)
```

Type chart sourced from `game-data-service` (`GET /api/typechart`) — cached locally on service startup.

### Status conditions

| Status | On attack | On end-of-turn | Duration |
|--------|-----------|----------------|----------|
| BRN | Halves physical attack | 1/16 max HP | Permanent |
| PSN | — | 1/8 max HP | Permanent |
| PAR | 25% skip chance, −50% speed | — | Permanent |
| SLP | Can't move | — | 1–3 turns (random on apply) |
| FRZ | Can't move | — | Permanent; 20% thaw per turn; thaw on Fire move hit |

### Priority

| Move | Priority |
|------|----------|
| Protect, Detect | +4 |
| Quick Attack | +1 |
| All others | 0 |
| Trick Room | −7 (reverses speed) |

Priority > speed > random tie-break.

### Multi-pod safety

Redis distributed lock (`SET NX EX 5`) ensures exactly one pod resolves each turn. Redis Pub/Sub fan-out ensures all pods (and their WebSocket clients) receive results.

### Tests

- `DamageCalculatorTest` — formula correctness for STAB, type matchups, crits
- `TurnResolutionServiceTest` — priority ordering, speed ordering, status application, faint detection
- `BattleServiceTest` — mock Redis + Kafka, verify state transitions

---

## rating-service

### Package structure

```
com.pokemon.rating
├── controller/   RatingController
├── service/      RatingService, EloCalculator
├── kafka/        BattleEndedConsumer
├── entity/       Rating, BattleHistory
├── repository/   RatingRepository, BattleHistoryRepository
├── dto/          BattleEndedEvent, RatingResponse, LeaderboardEntry
└── config/       KafkaConsumerConfig
```

### API

```
GET /api/ratings/{userId}/{format}     → {userId, format, elo, wins, losses}
GET /api/ratings/leaderboard/{format}  → [{rank, username, elo, wins, losses}] top 10
```

### Flyway migration `V2__ratings_schema.sql`

`username` is denormalized into `ratings` to avoid a cross-service join for the leaderboard. It is set on first INSERT and never updated (usernames are immutable in this MVP).

```sql
CREATE TABLE ratings (
    user_id  UUID REFERENCES users(id),
    username VARCHAR(50) NOT NULL,
    format   VARCHAR(50) NOT NULL,
    elo      INTEGER NOT NULL DEFAULT 1000,
    wins     INTEGER NOT NULL DEFAULT 0,
    losses   INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, format)
);

CREATE TABLE battle_history (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player1_id UUID REFERENCES users(id),
    player2_id UUID REFERENCES users(id),
    winner_id  UUID REFERENCES users(id),
    format     VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at   TIMESTAMPTZ DEFAULT NOW(),
    end_reason VARCHAR(50)   -- 'NORMAL' | 'FORFEIT' | 'DISCONNECT'
);
```

`rating-service` connects to the same PostgreSQL instance as `user-service` — no service-to-service HTTP calls needed for Elo.

### Elo flow (Kafka consumer `battle.ended`)

```
@KafkaListener(topics = "battle.ended")
1. SELECT * FROM ratings WHERE user_id IN (winnerId, loserId) AND format = ? FOR UPDATE
2. If rows missing: INSERT with elo=1000, username from event payload (battle.ended carries winner/loser usernames)
3. expected_winner = 1 / (1 + 10^((loserElo - winnerElo) / 400))
4. expected_loser  = 1 - expected_winner
5. winner_new_elo = winnerElo + K * (1 - expected_winner)   K=32
6. loser_new_elo  = loserElo  + K * (0 - expected_loser)
7. UPDATE ratings (both rows, same transaction)
8. INSERT INTO battle_history
```

### Tests

- `EloCalculatorTest` — formula correctness, symmetric K=32, equal-elo case
- `RatingServiceTest` — Kafka consumer mock, DB write verification

---

## Implementation Order

1. **Kafka infra** — add to docker-compose.yml + K8s, add spring-kafka to 3 pom.xml files
2. **lobby-service** — Redis queue, matchmaker scheduler, Kafka producer, polling endpoint
3. **battle-service** — match.found consumer, Redis state init, WebSocket STOMP, turn engine, battle.ended producer
4. **rating-service** — Flyway migration, battle.ended consumer, Elo update, leaderboard endpoint

---

## Non-Goals (Phase 2)

- Turn timer
- Spectator mode
- Replay storage
- Notifications
- Multiple formats beyond GEN9OU
- Complex abilities (weather setters, Intimidate, etc.)
- Items beyond Leftovers/Choice Band (basic stat modifiers)
