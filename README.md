# Poke-Battle

Competitive Pokémon battle platform — a full-stack clone of Pokémon Showdown built as a microservices system.

## Features

- **User accounts** — register, login (JWT), profile with match history and Elo rating
- **Team builder** — build and validate Gen 9 OU teams (6 Pokémon per team, format rules enforced)
- **Pokémon browser** — search the full Gen 9 OU Pokédex with base stats, types, and tier info
- **Matchmaking** — join a queue, get paired with an opponent in real time via Redis + Kafka
- **Live battles** — turn-based battles over WebSocket (STOMP); move selection, switching, type effectiveness, status conditions (BRN/PSN/PAR/SLP/FRZ), end-of-turn damage
- **Forfeit** — concede a battle at any time
- **Elo rating** — wins/losses update ratings via a Kafka-driven Elo calculator
- **Leaderboard** — top-ranked players ranked by Elo

## Stack

| Layer | Technology |
|---|---|
| Language | Java 17 · TypeScript |
| Backend framework | Spring Boot 3.3 (Maven multi-module) |
| Frontend | React 18 · Vite · TanStack Query · Zustand |
| Databases | PostgreSQL (users/teams/ratings) · MongoDB (game data) · Redis (battle state/queue) |
| Messaging | Apache Kafka (KRaft) |
| Gateway | Spring Cloud Gateway |
| Real-time | Spring WebSocket + STOMP |
| Infrastructure | Docker Compose · Kubernetes · Jenkins |

## Services

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | JWT auth filter, reverse proxy for all services |
| user-service | 8081 | Registration, login, team CRUD |
| game-data-service | 8082 | Pokémon / moves / items / formats catalog, team validation |
| lobby-service | 8083 | Matchmaking queue (Redis sorted set), matchmaker scheduler |
| battle-service | 8084 | WebSocket battle engine, turn resolution, Redis state |
| rating-service | 8085 | Elo calculator, leaderboard, Kafka consumer |
| frontend | 5175 | React SPA |

## Local Setup

### Prerequisites

- Docker and Docker Compose
- Java 17+
- Maven 3.9+
- Node.js 20+

### 1. Start infrastructure + build and run all services

```bash
# From repo root — builds all service JARs and starts everything
mvn package -DskipTests
docker compose -f infra/docker-compose.yml up --build -d
```

This starts: PostgreSQL, MongoDB, Redis, Kafka, and all 6 Spring Boot services.

### 2. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at **http://localhost:5175**

### 3. Verify services are healthy

```bash
docker compose -f infra/docker-compose.yml ps
```

All services should show `Up`. The `rating-service` may take a few extra seconds (Flyway migration runs on first start).

### 4. Seed check (optional)

Game data (Pokémon, moves, formats) is auto-seeded into MongoDB on first startup by `game-data-service`. To verify:

```bash
docker exec infra-mongodb-1 mongosh pokemon --quiet --eval "db.pokemon.countDocuments()"
```

## Playing a Battle

1. Open **http://localhost:5175** in two browser windows (use different users or incognito)
2. Register / log in in each window
3. Build a team of 6 Gen 9 OU Pokémon in each account (Team Builder → Save)
4. Click **Find Battle** in both windows and select your team
5. Both players will be matched and redirected to the battle screen
6. Select moves each turn — the turn resolves once both players submit
7. Battle ends when all Pokémon on one side faint, or a player forfeits

## Architecture Notes

- The API gateway validates JWTs and injects `X-User-Id` / `X-Username` headers — downstream services trust these headers, no JWT parsing downstream
- WebSocket connections pass the JWT as a `?token=` query parameter (browser limitation); the gateway accepts this for `/ws/**` paths
- Battle state is stored in Redis hashes (fast read/write per turn); Kafka events drive post-battle rating updates
- Flyway manages schema migrations for PostgreSQL; each service using Flyway has its own `flyway_schema_history_*` table to avoid collision on the shared database

## Running Tests

```bash
mvn test
```
