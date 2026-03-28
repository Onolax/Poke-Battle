# Poke-Battle

Competitive Pokemon battle platform — backend clone of Pokemon Showdown.

## Stack

- Java 21 · Spring Boot 3.3 · Maven multi-module
- PostgreSQL · MongoDB · Redis · Apache Kafka
- Spring Cloud Gateway · Spring WebSocket (STOMP)
- Docker · Kubernetes · Jenkins

## Services

| Service | Port | Role |
|---|---|---|
| api-gateway | 8080 | Reverse proxy, JWT auth filter |
| user-service | 8081 | Auth (local + OAuth2), team CRUD |
| game-data-service | 8082 | Pokémon/moves/items/formats catalog |
| lobby-service | 8083 | Matchmaking queue |
| battle-service | 8084 | Real-time WebSocket battles |
| rating-service | 8085 | Elo ratings |

## Local Setup

```bash
cd infra && docker compose up -d
mvn spring-boot:run -pl game-data-service
```
