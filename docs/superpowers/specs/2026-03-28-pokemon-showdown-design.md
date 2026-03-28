# Pokemon Showdown Clone — Design Spec

**Date:** 2026-03-28
**Status:** Approved

---

## Goal

A competitive Pokemon battle platform inspired by Pokemon Showdown. Two users can build teams, queue for a match, and play a real-time 1v1 turn-based battle. Backend-heavy; frontend minimal (React).

---

## Scope (MVP)

- Gen 9 only
- OU format only
- Auth: local (username/password) + Google/GitHub OAuth2
- Game data seeded from PokeAPI into MongoDB
- Frontend: minimal React (playable, not polished)

---

## Architecture: 6 Microservices

| Service | Port | Responsibility |
|---|---|---|
| `api-gateway` | 8080 | Reverse proxy, JWT filter, load balancing |
| `user-service` | 8081 | Auth, profiles, team CRUD |
| `game-data-service` | 8082 | Pokémon/moves/items/formats catalog |
| `lobby-service` | 8083 | Matchmaking queue, match pairing |
| `battle-service` | 8084 | WebSocket battles, turn resolution |
| `rating-service` | 8085 | Elo calculation, leaderboard |

All services: Java 21, Spring Boot 3.x, Maven multi-module.

---

## Data Stores

| Store | Version | Used By |
|---|---|---|
| PostgreSQL | 16 | user-service, rating-service |
| MongoDB | 7 | game-data-service |
| Redis | 7 | battle-service, lobby-service |
| Apache Kafka | 3.7 | lobby-service → battle-service → rating-service |

---

## Authentication

Spring Security OAuth2 + JWT.

- Local: POST `/auth/register`, POST `/auth/login` → returns JWT
- Social: Spring OAuth2 client (Google, GitHub) → callback → JWT issued
- JWT claims: `sub` (userId), `username`, `roles`, `exp`
- Gateway filter: validates JWT on every request, injects `X-User-Id` header downstream
- Services trust `X-User-Id` — do not re-validate token

---

## PostgreSQL Schema

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),           -- null for OAuth2-only
    oauth2_provider VARCHAR(50),          -- 'google' | 'github' | null
    oauth2_subject VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE teams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    format VARCHAR(50) NOT NULL DEFAULT 'GEN9OU',
    pokemon_data JSONB NOT NULL,          -- array of 6 pokemon
    is_validated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE battle_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player1_id UUID REFERENCES users(id),
    player2_id UUID REFERENCES users(id),
    winner_id UUID REFERENCES users(id),
    format VARCHAR(50) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    ended_at TIMESTAMPTZ,
    end_reason VARCHAR(50)                -- 'NORMAL' | 'FORFEIT' | 'TIMER' | 'DISCONNECT'
);

CREATE TABLE ratings (
    user_id UUID REFERENCES users(id),
    format VARCHAR(50) NOT NULL,
    elo INTEGER NOT NULL DEFAULT 1000,
    wins INTEGER NOT NULL DEFAULT 0,
    losses INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, format)
);
```

---

## MongoDB Schema

```js
// Collection: pokemon
{
  _id: "pikachu",                         // slug
  dex_number: 25,
  name: "Pikachu",
  types: ["Electric"],
  base_stats: { hp: 35, atk: 55, def: 40, spa: 50, spd: 50, spe: 90 },
  abilities: ["Static", "Lightning Rod"],
  learnset: { gen9: ["thunderbolt", "volt-tackle", "iron-tail"] },
  tier: { gen9ou: "NU" },
  weight_kg: 6.0
}

// Collection: moves
{
  _id: "surf",
  name: "Surf",
  type: "Water",
  category: "Special",                    // Physical | Special | Status
  base_power: 90,
  accuracy: 100,
  pp: 15,
  priority: 0,
  secondary_effect: null                  // or { chance: 30, effect: "BURN" }
}

// Collection: items
{
  _id: "leftovers",
  name: "Leftovers",
  effect: "END_OF_TURN_HEAL",
  param: 0.0625                           // 1/16 max HP
}

// Collection: formats
{
  _id: "gen9ou",
  name: "Gen 9 OU",
  generation: 9,
  banned_pokemon: ["flutter-mane", "iron-bundle"],
  banned_moves: ["swagger"],
  team_size: 6,
  active_size: 1
}
```

---

## Redis Structures

```
# Battle state
battle:{id}:state         Hash  { phase, turnNumber, weather, activeSlot1, activeSlot2, player1Id, player2Id }
battle:{id}:team:p1       String (JSON array of 6 pokemon with current HP, status, PP, stat boosts)
battle:{id}:team:p2       String
battle:{id}:moves:pending Hash  { player1: "surf" | null, player2: "earthquake" | null }
battle:{id}:events        Pub/Sub channel

# Matchmaking
queue:GEN9OU              Sorted Set  { member: "{userId}:{teamId}", score: timestamp }

# TTL
All battle:* keys: 2 hour TTL
Active battle state reset to 2h on each turn
```

---

## Kafka Topics

| Topic | Producer | Consumer | Payload |
|---|---|---|---|
| `match.found` | lobby-service | battle-service | `{ battleId, player1Id, player1TeamId, player2Id, player2TeamId, format }` |
| `battle.started` | battle-service | (notification-service Phase 4) | `{ battleId, player1Id, player2Id }` |
| `battle.ended` | battle-service | rating-service | `{ battleId, winnerId, loserId, format, turns }` |

---

## Key Data Flows

### Team Building
```
POST /api/teams  (JWT required)
  → api-gateway (validate JWT, inject X-User-Id)
  → user-service
      → GET /api/pokemon/validate-team  (call game-data-service)
      → INSERT INTO teams (postgres)
  ← 201 { teamId }
```

### Matchmaking
```
POST /api/lobby/queue  { teamId, format }
  → lobby-service
      → ZADD queue:GEN9OU {timestamp} {userId}:{teamId}
      → scheduler: ZRANGE queue:GEN9OU 0 1 (every 1s)
      → when 2 found: ZREM both (Lua script, atomic)
      → generate battleId (UUID)
      → Kafka: match.found
  → battle-service consumes match.found
      → init Redis battle state
      → SET battle:{id}:state, team:p1, team:p2

GET /api/lobby/status  (poll until battleId returned)
  → lobby-service checks Redis: lobby:user:{userId}:battleId
  ← { battleId } or { waiting: true }
```

### Battle Turn
```
WS connect: /ws/battle/{battleId}?token={JWT}
  → api-gateway routes to battle-service (hash on battleId for consistent routing)
  → battle-service validates JWT on handshake
  → pod subscribes to Redis channel battle:{battleId}:events

STOMP send: /app/battle/{battleId}/move  { move: "surf" }
  → pod validates: is it this user's turn? is move legal? timer ok?
  → HSETNX battle:{battleId}:moves:pending player1 "surf"
  → HLEN == 2?
      → yes: acquire Redis lock (SET battle:{id}:lock {podId} NX EX 5)
             resolve turn (damage calc, priority, speed, secondary effects)
             update battle:{id}:state and team JSON
             DEL lock
             PUBLISH battle:{id}:events { type: TURN_RESULT, ... }
  → all pods subscribed receive pub/sub
  → each pod pushes to locally connected WS sessions: /topic/battle/{battleId}
```

### Post-Battle
```
battle-service detects win condition (all HP=0, forfeit)
  → Kafka: battle.ended { battleId, winnerId, loserId, format }
  → WS: send final result to both clients
  → SET TTL on battle:* keys to 5 min

rating-service consumes battle.ended
  → SELECT elo FROM ratings WHERE user_id IN (winnerId, loserId) FOR UPDATE
  → calculate Elo delta (K=32 standard)
  → UPDATE ratings (both rows, single transaction)
  → INSERT INTO battle_history
```

---

## API Gateway Config

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth
          uri: lb://user-service
          predicates:
            - Path=/auth/**

        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**, /api/teams/**

        - id: game-data-service
          uri: lb://game-data-service
          predicates:
            - Path=/api/pokemon/**, /api/moves/**, /api/items/**, /api/formats/**

        - id: lobby-service
          uri: lb://lobby-service
          predicates:
            - Path=/api/lobby/**

        - id: battle-service-http
          uri: lb://battle-service
          predicates:
            - Path=/api/battle/**

        - id: battle-service-ws
          uri: lb://battle-service
          predicates:
            - Path=/ws/battle/**
          metadata:
            response-timeout: 0ms
```

JWT global filter: validates token, injects `X-User-Id` and `X-Username` headers. Passes `/auth/**` without validation.

---

## Battle Engine Rules (Gen 9 OU)

- Damage formula: `((2*level/5 + 2) * power * atk/def) / 50 + 2 * modifier`
- Modifier includes: STAB (1.5x), type effectiveness, random factor (0.85-1.0), critical hit (1.5x, ~4% chance)
- Speed determines move order (ties broken randomly)
- Priority moves go first (Quick Attack +1, Protect +4, etc.)
- Status conditions: BRN (halves physical atk, 1/16 HP loss/turn), PSN (1/8 HP/turn), PAR (25% chance to skip, -50% speed), SLP (1-3 turns), FRZ (thaw on fire move)
- Switch action: player can switch instead of move (costs turn, no damage)
- Faint: when HP = 0, must switch; if no Pokemon left, battle ends

---

## Type Effectiveness Chart

Stored in MongoDB as a format document or a separate `type_chart` collection:

```js
{
  _id: "type_chart_gen9",
  chart: {
    "Water":   { "Fire": 2.0, "Rock": 2.0, "Grass": 0.5, "Water": 0.5, "Dragon": 0.5 },
    "Fire":    { "Grass": 2.0, "Ice": 2.0, "Bug": 2.0, "Steel": 2.0, "Water": 0.5, ... },
    // ... all 18 types
  }
}
```

---

## Build Phases

### Phase 1 — Playable (no Kafka)
- All 6 services bootstrapped (Spring Initializr, Maven parent POM)
- game-data-service: PokeAPI seed script + MongoDB CRUD
- user-service: local auth + OAuth2 + JWT + team CRUD
- api-gateway: routing + JWT filter
- lobby-service: Redis queue + polling endpoint
- battle-service: WebSocket + Redis state + turn resolution + Redis pub/sub
- rating-service: synchronous Elo update (direct Postgres write from battle-service for now)
- Frontend: React app with login, team builder, queue button, battle UI

### Phase 2 — Async
- Add Kafka (Docker Compose)
- battle.ended event → rating-service consumer
- battle.started event
- Add replay storage (MongoDB)

### Phase 3 — DevOps
- Docker Compose: all services + infra
- Kubernetes manifests: Deployments, Services, ConfigMaps, Secrets
- HPA on battle-service
- Jenkins pipeline: build → test → Docker build → push → deploy

### Phase 4 — Extensions
- Notifications service
- Spectator support
- Challenge (direct invite) system
- Replay playback

---

## Project Structure

```
pokemon/
  pom.xml                    (parent POM)
  api-gateway/
  user-service/
  game-data-service/
  lobby-service/
  battle-service/
  rating-service/
  frontend/                  (React, separate from Maven)
  docs/
    superpowers/
      specs/
      plans/
  infra/
    docker-compose.yml
    k8s/
```

---

## Non-Goals (MVP)

- Replays
- Spectators
- Chat
- Notifications
- Multiple generations
- Abilities with complex effects (weather setters, etc.)
- Items beyond basic (Leftovers, Choice items)
