# Pokemon Showdown — Phase 1, Plan 1: Foundation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Scaffold entire Maven multi-module project, stand up local infra via Docker Compose, and deliver a fully operational game-data-service with Pokemon/move/item data seeded from PokeAPI.

**Architecture:** Maven parent POM manages 6 Spring Boot service modules. Docker Compose runs Postgres 16, MongoDB 7, Redis 7. game-data-service owns all read-only game data in MongoDB, exposed via REST. All other services are stubs in Plan 1.

**Tech Stack:** Java 21, Spring Boot 3.3.4, MongoDB, Testcontainers, Lombok, RestClient (Spring 6.1)

---

## Sub-plan map (Phase 1)

- **Plan 1 (this):** Maven scaffold + Docker Compose + game-data-service ← YOU ARE HERE
- Plan 2: user-service (local auth + OAuth2 + JWT + team CRUD) + api-gateway
- Plan 3: lobby-service (Redis matchmaking) + battle-service (WebSocket + turns + pub/sub)
- Plan 4: rating-service (Elo) + React frontend

**Deliverable for this plan:** `mvn test -pl game-data-service` green; `GET http://localhost:8082/api/pokemon/great-tusk` returns stats; Docker Compose up cleanly.

---

## File Structure

```
pokemon/
  pom.xml                                        ← parent, manages all deps
  infra/
    docker-compose.yml
  game-data-service/
    pom.xml
    src/main/java/com/pokemon/gamedata/
      GameDataServiceApplication.java
      domain/
        Pokemon.java                             ← @Document, @Data
        BaseStats.java                           ← embedded
        Move.java
        Item.java
        Format.java
        TypeChart.java
      repository/
        PokemonRepository.java
        MoveRepository.java
        ItemRepository.java
        FormatRepository.java
        TypeChartRepository.java
      client/
        PokeApiClient.java                       ← RestClient wrapper
        dto/PokeApiPokemon.java                  ← record
        dto/PokeApiMove.java                     ← record
      seed/
        SeedData.java                            ← static slug lists, tiers, learnsets
        SeedRunner.java                          ← ApplicationRunner, fetches + saves
      service/
        PokemonService.java
        MoveService.java
        ValidationService.java                   ← validates team vs format rules
      controller/
        PokemonController.java
        MoveController.java
        ValidationController.java
    src/main/resources/application.yml
    src/test/java/com/pokemon/gamedata/
      repository/PokemonRepositoryTest.java      ← @DataMongoTest + Testcontainers
      controller/PokemonControllerTest.java      ← @WebMvcTest + @MockBean
      service/ValidationServiceTest.java         ← pure unit test
    src/test/resources/application-test.yml
  api-gateway/         ← stub
  user-service/        ← stub
  lobby-service/       ← stub
  battle-service/      ← stub
  rating-service/      ← stub
```

---

## Task 1: Git init + Maven parent POM

**Files:**
- Create: `pokemon/.gitignore`
- Create: `pokemon/pom.xml`

- [ ] **Step 1: Init git**

```bash
cd /home/monu/programming/java/pokemon
git init
```

Expected: `Initialized empty Git repository in .../pokemon/.git/`

- [ ] **Step 2: Create .gitignore**

Create `pokemon/.gitignore`:
```
target/
*.class
*.jar
*.war
.idea/
*.iml
.DS_Store
.env
```

- [ ] **Step 3: Write parent pom.xml**

Create `pokemon/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>com.pokemon</groupId>
    <artifactId>pokemon-parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>api-gateway</module>
        <module>user-service</module>
        <module>game-data-service</module>
        <module>lobby-service</module>
        <module>battle-service</module>
        <module>rating-service</module>
    </modules>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2023.0.3</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 4: Verify parent parses**

```bash
cd /home/monu/programming/java/pokemon
mvn help:effective-pom -N
```

Expected: output containing `<artifactId>pokemon-parent</artifactId>` with no errors. (Modules will fail until created — ignore those.)

- [ ] **Step 5: Commit**

```bash
git add pom.xml .gitignore
git commit -m "init: parent pom, java 21, spring boot 3.3.4"
```

---

## Task 2: Service stubs (5 services)

**Files:** One `pom.xml` + one `Application.java` + one `application.yml` per service (api-gateway, user-service, lobby-service, battle-service, rating-service).

- [ ] **Step 1: Create directory structure**

```bash
for svc in api-gateway user-service lobby-service battle-service rating-service; do
  mkdir -p /home/monu/programming/java/pokemon/$svc/src/main/resources
done
```

- [ ] **Step 2: Create pom.xml for each stub service**

Create `api-gateway/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pokemon</groupId>
        <artifactId>pokemon-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>api-gateway</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Create `user-service/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pokemon</groupId>
        <artifactId>pokemon-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>user-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Create `lobby-service/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pokemon</groupId>
        <artifactId>pokemon-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>lobby-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Create `battle-service/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pokemon</groupId>
        <artifactId>pokemon-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>battle-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

Create `rating-service/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pokemon</groupId>
        <artifactId>pokemon-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>rating-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Create Application.java for each stub**

Create `api-gateway/src/main/java/com/pokemon/gateway/ApiGatewayApplication.java`:
```java
package com.pokemon.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

Create `api-gateway/src/main/resources/application.yml`:
```yaml
server:
  port: 8080
spring:
  application:
    name: api-gateway
```

Repeat the pattern above for each stub service — only the package name, class name, port, and app name change:

| Service | Package | Class | Port |
|---|---|---|---|
| user-service | com.pokemon.user | UserServiceApplication | 8081 |
| lobby-service | com.pokemon.lobby | LobbyServiceApplication | 8083 |
| battle-service | com.pokemon.battle | BattleServiceApplication | 8084 |
| rating-service | com.pokemon.rating | RatingServiceApplication | 8085 |

- [ ] **Step 4: Verify parent build resolves all modules**

```bash
cd /home/monu/programming/java/pokemon
mvn validate
```

Expected: `BUILD SUCCESS` with no errors. (Tests will be skipped since stubs have none.)

- [ ] **Step 5: Commit**

```bash
git add api-gateway/ user-service/ lobby-service/ battle-service/ rating-service/
git commit -m "chore: service stubs, all 6 modules"
```

---

## Task 3: Docker Compose (infra)

**Files:**
- Create: `infra/docker-compose.yml`

- [ ] **Step 1: Write failing test (smoke check)**

Create `infra/test-infra.sh`:
```bash
#!/bin/bash
# Smoke test: checks all 3 services are reachable after compose up
set -e
echo "Testing Postgres..."
docker exec pokemon-postgres pg_isready -U pokemon
echo "Testing MongoDB..."
docker exec pokemon-mongo mongosh --eval "db.adminCommand('ping')" --quiet
echo "Testing Redis..."
docker exec pokemon-redis redis-cli ping
echo "All infra OK"
```

- [ ] **Step 2: Run smoke test — expect failure (containers not running yet)**

```bash
chmod +x infra/test-infra.sh
bash infra/test-infra.sh
```

Expected: error `No such container: pokemon-postgres`

- [ ] **Step 3: Write docker-compose.yml**

Create `infra/docker-compose.yml`:
```yaml
version: '3.9'

services:
  postgres:
    image: postgres:16
    container_name: pokemon-postgres
    environment:
      POSTGRES_DB: pokemon
      POSTGRES_USER: pokemon
      POSTGRES_PASSWORD: pokemon
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U pokemon"]
      interval: 5s
      timeout: 5s
      retries: 5

  mongo:
    image: mongo:7.0
    container_name: pokemon-mongo
    environment:
      MONGO_INITDB_DATABASE: pokemon
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')", "--quiet"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7.2
    container_name: pokemon-redis
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
  mongo_data:
```

- [ ] **Step 4: Start infra**

```bash
cd /home/monu/programming/java/pokemon/infra
docker compose up -d
```

Wait ~10 seconds for healthchecks to pass, then:

```bash
docker compose ps
```

Expected:
```
NAME               STATUS
pokemon-mongo      running (healthy)
pokemon-postgres   running (healthy)
pokemon-redis      running (healthy)
```

- [ ] **Step 5: Run smoke test — expect pass**

```bash
bash test-infra.sh
```

Expected:
```
Testing Postgres...
/var/run/postgresql:5432 - accepting connections
Testing MongoDB...
{ ok: 1 }
Testing Redis...
PONG
All infra OK
```

- [ ] **Step 6: Commit**

```bash
cd /home/monu/programming/java/pokemon
git add infra/
git commit -m "chore: docker compose - postgres, mongodb, redis"
```

---

## Task 4: game-data-service — pom.xml + domain model

**Files:**
- Create: `game-data-service/pom.xml`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/GameDataServiceApplication.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/domain/Pokemon.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/domain/BaseStats.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/domain/Move.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/domain/Item.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/domain/Format.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/domain/TypeChart.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/repository/*.java` (5 files)
- Test: `game-data-service/src/test/java/com/pokemon/gamedata/repository/PokemonRepositoryTest.java`

- [ ] **Step 1: Write failing repository test**

Create directory:
```bash
mkdir -p game-data-service/src/test/java/com/pokemon/gamedata/repository
mkdir -p game-data-service/src/test/resources
```

Create `game-data-service/src/test/java/com/pokemon/gamedata/repository/PokemonRepositoryTest.java`:
```java
package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.BaseStats;
import com.pokemon.gamedata.domain.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class PokemonRepositoryTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    PokemonRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void savesAndFindsById() {
        Pokemon p = new Pokemon();
        p.setId("pikachu");
        p.setDexNumber(25);
        p.setName("Pikachu");
        p.setTypes(List.of("Electric"));
        p.setBaseStats(new BaseStats(35, 55, 40, 50, 50, 90));
        p.setAbilities(List.of("Static", "Lightning Rod"));
        p.setLearnset(List.of("thunderbolt", "volt-tackle"));
        p.setTier(Map.of("gen9ou", "NU"));
        p.setWeightKg(6.0);

        repository.save(p);

        Optional<Pokemon> found = repository.findById("pikachu");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Pikachu");
        assertThat(found.get().getBaseStats().getSpe()).isEqualTo(90);
        assertThat(found.get().getTier()).containsEntry("gen9ou", "NU");
    }

    @Test
    void findsByTier() {
        Pokemon ou = new Pokemon();
        ou.setId("garchomp");
        ou.setName("Garchomp");
        ou.setTier(Map.of("gen9ou", "OU"));
        repository.save(ou);

        Pokemon nu = new Pokemon();
        nu.setId("pikachu");
        nu.setName("Pikachu");
        nu.setTier(Map.of("gen9ou", "NU"));
        repository.save(nu);

        List<Pokemon> ouPokemon = repository.findByTierGen9ou("OU");
        assertThat(ouPokemon).hasSize(1);
        assertThat(ouPokemon.get(0).getId()).isEqualTo("garchomp");
    }
}
```

- [ ] **Step 2: Run test — expect compilation failure**

```bash
cd /home/monu/programming/java/pokemon
mvn test -pl game-data-service 2>&1 | head -30
```

Expected: compilation error — `game-data-service/src/main/java/...` classes don't exist yet.

- [ ] **Step 3: Write game-data-service/pom.xml**

Create `game-data-service/pom.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.pokemon</groupId>
        <artifactId>pokemon-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>game-data-service</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mongodb</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 4: Create Application main class**

```bash
mkdir -p game-data-service/src/main/java/com/pokemon/gamedata/domain
mkdir -p game-data-service/src/main/java/com/pokemon/gamedata/repository
mkdir -p game-data-service/src/main/java/com/pokemon/gamedata/client/dto
mkdir -p game-data-service/src/main/java/com/pokemon/gamedata/seed
mkdir -p game-data-service/src/main/java/com/pokemon/gamedata/service
mkdir -p game-data-service/src/main/java/com/pokemon/gamedata/controller
mkdir -p game-data-service/src/main/resources
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/GameDataServiceApplication.java`:
```java
package com.pokemon.gamedata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GameDataServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameDataServiceApplication.class, args);
    }
}
```

- [ ] **Step 5: Create domain classes**

Create `game-data-service/src/main/java/com/pokemon/gamedata/domain/BaseStats.java`:
```java
package com.pokemon.gamedata.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseStats {
    private int hp;
    private int atk;
    private int def;
    private int spa;
    private int spd;
    private int spe;
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/domain/Pokemon.java`:
```java
package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "pokemon")
public class Pokemon {
    @Id
    private String id;           // slug e.g. "garchomp"
    private int dexNumber;
    private String name;
    private List<String> types;
    private BaseStats baseStats;
    private List<String> abilities;
    private List<String> learnset;         // move slugs available in gen9
    private Map<String, String> tier;      // { "gen9ou": "OU" }
    private double weightKg;
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/domain/Move.java`:
```java
package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "moves")
public class Move {
    @Id
    private String id;           // slug e.g. "earthquake"
    private String name;
    private String type;         // "Ground", "Fire", etc.
    private String category;     // "Physical" | "Special" | "Status"
    private Integer basePower;   // null for status moves
    private Integer accuracy;    // null for never-miss moves
    private int pp;
    private int priority;
    private SecondaryEffect secondaryEffect;  // null if none

    @Data
    public static class SecondaryEffect {
        private int chance;       // percentage, e.g. 30
        private String effect;    // "BURN" | "PARALYSIS" | etc.
    }
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/domain/Item.java`:
```java
package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "items")
public class Item {
    @Id
    private String id;      // slug e.g. "leftovers"
    private String name;
    private String effect;  // "END_OF_TURN_HEAL" | "SPEED_BOOST" | etc.
    private double param;   // numeric param for the effect (e.g. 0.0625 = 1/16)
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/domain/Format.java`:
```java
package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "formats")
public class Format {
    @Id
    private String id;                   // "gen9ou"
    private String name;                 // "Gen 9 OU"
    private int generation;
    private List<String> bannedPokemon;
    private List<String> bannedMoves;
    private int teamSize;
    private int activeSize;
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/domain/TypeChart.java`:
```java
package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "type_charts")
public class TypeChart {
    @Id
    private String id;    // "type_chart_gen9"
    // chart[attackType][defenseType] = multiplier
    // e.g. chart["Water"]["Fire"] = 2.0
    private Map<String, Map<String, Double>> chart;
}
```

- [ ] **Step 6: Create repositories**

Create `game-data-service/src/main/java/com/pokemon/gamedata/repository/PokemonRepository.java`:
```java
package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Pokemon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PokemonRepository extends MongoRepository<Pokemon, String> {
    @Query("{ 'tier.gen9ou': ?0 }")
    List<Pokemon> findByTierGen9ou(String tier);
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/repository/MoveRepository.java`:
```java
package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Move;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MoveRepository extends MongoRepository<Move, String> {
    List<Move> findByType(String type);
    List<Move> findByCategory(String category);
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/repository/ItemRepository.java`:
```java
package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String> {
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/repository/FormatRepository.java`:
```java
package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Format;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FormatRepository extends MongoRepository<Format, String> {
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/repository/TypeChartRepository.java`:
```java
package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.TypeChart;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TypeChartRepository extends MongoRepository<TypeChart, String> {
}
```

- [ ] **Step 7: Create application.yml**

Create `game-data-service/src/main/resources/application.yml`:
```yaml
server:
  port: 8082
spring:
  application:
    name: game-data-service
  data:
    mongodb:
      uri: mongodb://localhost:27017/pokemon
```

Create `game-data-service/src/test/resources/application-test.yml`:
```yaml
# Testcontainers overrides spring.data.mongodb.uri via @DynamicPropertySource
# No additional config needed
```

- [ ] **Step 8: Run the failing test — expect RED (test should fail, not error)**

```bash
cd /home/monu/programming/java/pokemon
mvn test -pl game-data-service
```

Expected: Tests compile and run. `savesAndFindsById` passes. `findsByTier` FAILS with:
```
Expected: 1 element(s)
but was: 0 element(s)
```
This confirms `findByTierGen9ou` query is not working yet.

- [ ] **Step 9: Fix the `@Query` on `findByTierGen9ou`**

The query `{ 'tier.gen9ou': ?0 }` queries the nested map key. This is correct MongoDB syntax. If failing, check that MongoDB container started and `tier` field is a `Map<String, String>`.

Verify by adding a `@BeforeEach` print in the test to confirm the document structure was saved correctly. The `@Query` annotation on `findByTierGen9ou` should work with the `Map<String, String> tier` field in MongoDB as dot-notation access.

If still failing, replace `@Query` with a `MongoTemplate` approach in a `PokemonRepositoryCustom` implementation:
```java
// Only if @Query doesn't work — skip if tests pass
List<Pokemon> findByTierGen9ou(String tier);
// Add to PokemonRepository instead:
@Query("{ 'tier.gen9ou' : ?0 }")
List<Pokemon> findByGen9ouTier(String tier);
```

- [ ] **Step 10: Run tests — expect GREEN**

```bash
mvn test -pl game-data-service
```

Expected:
```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 11: Commit**

```bash
git add game-data-service/
git commit -m "feat(game-data): domain model, repositories, testcontainers tests"
```

---

## Task 5: game-data-service — PokeAPI client + SeedData

**Files:**
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/client/dto/PokeApiPokemon.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/client/dto/PokeApiMove.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/client/PokeApiClient.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/seed/SeedData.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/seed/SeedRunner.java`

- [ ] **Step 1: Write failing test for PokeApiClient**

Create `game-data-service/src/test/java/com/pokemon/gamedata/client/PokeApiClientTest.java`:
```java
package com.pokemon.gamedata.client;

import com.pokemon.gamedata.client.dto.PokeApiPokemon;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PokeApiClientTest {

    @Test
    void fetchesPokemonBySlug() {
        RestClient mockRestClient = mock(RestClient.class);
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        PokeApiPokemon expected = new PokeApiPokemon(
            6, "charizard", List.of(), List.of(), List.of(), 905
        );

        when(mockRestClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/pokemon/{slug}", "charizard")).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PokeApiPokemon.class)).thenReturn(expected);

        PokeApiClient client = new PokeApiClient(mockRestClient);
        PokeApiPokemon result = client.fetchPokemon("charizard");

        assertThat(result.name()).isEqualTo("charizard");
        assertThat(result.id()).isEqualTo(6);
    }
}
```

- [ ] **Step 2: Run test — expect compilation failure (classes not yet written)**

```bash
mvn test -pl game-data-service -Dtest=PokeApiClientTest 2>&1 | tail -5
```

Expected: compilation error — `PokeApiClient`, `PokeApiPokemon` don't exist.

- [ ] **Step 3: Create DTO records**

Create `game-data-service/src/main/java/com/pokemon/gamedata/client/dto/PokeApiPokemon.java`:
```java
package com.pokemon.gamedata.client.dto;

import java.util.List;

public record PokeApiPokemon(
    int id,
    String name,
    List<StatEntry> stats,
    List<TypeEntry> types,
    List<AbilityEntry> abilities,
    int weight
) {
    public record StatEntry(int base_stat, StatName stat) {}
    public record StatName(String name) {}
    public record TypeEntry(int slot, TypeName type) {}
    public record TypeName(String name) {}
    public record AbilityEntry(AbilityName ability, boolean is_hidden) {}
    public record AbilityName(String name) {}

    public int statValue(String statName) {
        return stats.stream()
            .filter(s -> s.stat().name().equals(statName))
            .mapToInt(StatEntry::base_stat)
            .findFirst()
            .orElse(0);
    }
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/client/dto/PokeApiMove.java`:
```java
package com.pokemon.gamedata.client.dto;

public record PokeApiMove(
    String name,
    Integer power,
    int pp,
    Integer accuracy,
    int priority,
    DamageClass damage_class,
    TypeName type
) {
    public record DamageClass(String name) {}
    public record TypeName(String name) {}
}
```

- [ ] **Step 4: Create PokeApiClient**

Create `game-data-service/src/main/java/com/pokemon/gamedata/client/PokeApiClient.java`:
```java
package com.pokemon.gamedata.client;

import com.pokemon.gamedata.client.dto.PokeApiMove;
import com.pokemon.gamedata.client.dto.PokeApiPokemon;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PokeApiClient {

    private final RestClient restClient;

    public PokeApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public PokeApiPokemon fetchPokemon(String slug) {
        return restClient.get()
            .uri("/pokemon/{slug}", slug)
            .retrieve()
            .body(PokeApiPokemon.class);
    }

    public PokeApiMove fetchMove(String slug) {
        return restClient.get()
            .uri("/move/{slug}", slug)
            .retrieve()
            .body(PokeApiMove.class);
    }
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/config/PokeApiConfig.java`:
```java
package com.pokemon.gamedata.config;

import com.pokemon.gamedata.client.PokeApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PokeApiConfig {

    @Bean
    public RestClient pokeApiRestClient() {
        return RestClient.builder()
            .baseUrl("https://pokeapi.co/api/v2")
            .build();
    }

    @Bean
    public PokeApiClient pokeApiClient(RestClient pokeApiRestClient) {
        return new PokeApiClient(pokeApiRestClient);
    }
}
```

- [ ] **Step 5: Run test — expect GREEN**

```bash
mvn test -pl game-data-service -Dtest=PokeApiClientTest
```

Expected: `Tests run: 1, Failures: 0`

- [ ] **Step 6: Create SeedData (static lists — no API calls)**

Create `game-data-service/src/main/java/com/pokemon/gamedata/seed/SeedData.java`:
```java
package com.pokemon.gamedata.seed;

import java.util.List;
import java.util.Map;

/**
 * Static seed data: which Pokemon/moves/items to fetch and their tier/learnset assignments.
 * Learnsets are curated competitive subsets — not full Gen9 learnsets.
 */
public final class SeedData {

    private SeedData() {}

    /** Pokemon slugs to fetch from PokeAPI. Must be valid PokeAPI slugs. */
    public static final List<String> POKEMON_SLUGS = List.of(
        "garchomp", "great-tusk", "iron-hands", "gholdengo", "kingambit",
        "dragapult", "roaring-moon", "iron-valiant", "volcarona", "garganacl",
        "ting-lu", "clodsire", "skeledirge", "meowscarada", "iron-moth",
        "glimmora", "corviknight", "hatterene", "rillaboom", "toxapex"
    );

    /** Move slugs to fetch from PokeAPI. */
    public static final List<String> MOVE_SLUGS = List.of(
        "earthquake", "thunderbolt", "flamethrower", "ice-beam", "surf",
        "shadow-ball", "close-combat", "knock-off", "u-turn", "protect",
        "stealth-rock", "recover", "will-o-wisp", "toxic", "swords-dance",
        "nasty-plot", "calm-mind", "body-press", "draco-meteor", "spore"
    );

    /** Item slugs to fetch from PokeAPI. */
    public static final List<String> ITEM_SLUGS = List.of(
        "leftovers", "choice-scarf", "choice-specs", "choice-band",
        "life-orb", "rocky-helmet", "heavy-duty-boots", "focus-sash",
        "assault-vest", "eviolite"
    );

    /** Tier assignments for Gen9 OU. Key = pokemon slug, value = tier string. */
    public static final Map<String, String> GEN9OU_TIERS = Map.ofEntries(
        Map.entry("garchomp",     "OU"),
        Map.entry("great-tusk",   "OU"),
        Map.entry("iron-hands",   "OU"),
        Map.entry("gholdengo",    "OU"),
        Map.entry("kingambit",    "OU"),
        Map.entry("dragapult",    "OU"),
        Map.entry("roaring-moon", "OU"),
        Map.entry("iron-valiant", "OU"),
        Map.entry("volcarona",    "OU"),
        Map.entry("garganacl",    "OU"),
        Map.entry("ting-lu",      "OU"),
        Map.entry("clodsire",     "OU"),
        Map.entry("skeledirge",   "OU"),
        Map.entry("meowscarada",  "OU"),
        Map.entry("iron-moth",    "OU"),
        Map.entry("glimmora",     "OU"),
        Map.entry("corviknight",  "OU"),
        Map.entry("hatterene",    "OU"),
        Map.entry("rillaboom",    "OU"),
        Map.entry("toxapex",      "OU")
    );

    /** Curated competitive learnsets per pokemon slug. */
    public static final Map<String, List<String>> LEARNSETS = Map.ofEntries(
        Map.entry("garchomp",     List.of("earthquake", "swords-dance", "stealth-rock", "u-turn")),
        Map.entry("great-tusk",   List.of("earthquake", "close-combat", "knock-off", "u-turn", "stealth-rock", "body-press")),
        Map.entry("iron-hands",   List.of("close-combat", "thunderbolt", "swords-dance", "u-turn")),
        Map.entry("gholdengo",    List.of("shadow-ball", "nasty-plot", "recover", "thunderbolt")),
        Map.entry("kingambit",    List.of("swords-dance", "knock-off", "close-combat")),
        Map.entry("dragapult",    List.of("shadow-ball", "u-turn", "thunderbolt", "flamethrower", "draco-meteor")),
        Map.entry("roaring-moon", List.of("u-turn", "knock-off", "swords-dance", "draco-meteor")),
        Map.entry("iron-valiant", List.of("close-combat", "thunderbolt", "nasty-plot", "calm-mind", "knock-off")),
        Map.entry("volcarona",    List.of("flamethrower", "calm-mind", "recover")),
        Map.entry("garganacl",    List.of("recover", "stealth-rock", "will-o-wisp", "body-press")),
        Map.entry("ting-lu",      List.of("earthquake", "stealth-rock", "toxic", "knock-off")),
        Map.entry("clodsire",     List.of("toxic", "stealth-rock", "earthquake", "recover")),
        Map.entry("skeledirge",   List.of("flamethrower", "shadow-ball", "recover", "will-o-wisp")),
        Map.entry("meowscarada",  List.of("knock-off", "u-turn", "swords-dance")),
        Map.entry("iron-moth",    List.of("flamethrower", "nasty-plot", "recover", "thunderbolt")),
        Map.entry("glimmora",     List.of("stealth-rock", "toxic", "flamethrower")),
        Map.entry("corviknight",  List.of("u-turn", "stealth-rock", "body-press", "recover")),
        Map.entry("hatterene",    List.of("calm-mind", "recover", "nasty-plot", "shadow-ball")),
        Map.entry("rillaboom",    List.of("u-turn", "knock-off", "swords-dance", "earthquake")),
        Map.entry("toxapex",      List.of("toxic", "recover", "surf", "will-o-wisp"))
    );

    /** Full Gen9 type effectiveness chart. chart[attackingType][defendingType] = multiplier */
    public static final Map<String, Map<String, Double>> TYPE_CHART = Map.ofEntries(
        Map.entry("Normal",   Map.of("Rock", 0.5, "Ghost", 0.0, "Steel", 0.5)),
        Map.entry("Fire",     Map.of("Fire", 0.5, "Water", 0.5, "Grass", 2.0, "Ice", 2.0, "Bug", 2.0, "Rock", 0.5, "Dragon", 0.5, "Steel", 2.0)),
        Map.entry("Water",    Map.of("Fire", 2.0, "Water", 0.5, "Grass", 0.5, "Ground", 2.0, "Rock", 2.0, "Dragon", 0.5)),
        Map.entry("Electric", Map.of("Water", 2.0, "Electric", 0.5, "Grass", 0.5, "Ground", 0.0, "Flying", 2.0, "Dragon", 0.5)),
        Map.entry("Grass",    Map.of("Fire", 0.5, "Water", 2.0, "Grass", 0.5, "Poison", 0.5, "Ground", 2.0, "Flying", 0.5, "Bug", 0.5, "Rock", 2.0, "Dragon", 0.5, "Steel", 0.5)),
        Map.entry("Ice",      Map.of("Fire", 0.5, "Water", 0.5, "Grass", 2.0, "Ice", 0.5, "Ground", 2.0, "Flying", 2.0, "Dragon", 2.0, "Steel", 0.5)),
        Map.entry("Fighting", Map.of("Normal", 2.0, "Ice", 2.0, "Poison", 0.5, "Flying", 0.5, "Psychic", 0.5, "Bug", 0.5, "Rock", 2.0, "Ghost", 0.0, "Dark", 2.0, "Steel", 2.0, "Fairy", 0.5)),
        Map.entry("Poison",   Map.of("Grass", 2.0, "Poison", 0.5, "Ground", 0.5, "Rock", 0.5, "Ghost", 0.5, "Steel", 0.0, "Fairy", 2.0)),
        Map.entry("Ground",   Map.of("Fire", 2.0, "Electric", 2.0, "Grass", 0.5, "Poison", 2.0, "Flying", 0.0, "Bug", 0.5, "Rock", 2.0, "Steel", 2.0)),
        Map.entry("Flying",   Map.of("Electric", 0.5, "Grass", 2.0, "Fighting", 2.0, "Bug", 2.0, "Rock", 0.5, "Steel", 0.5)),
        Map.entry("Psychic",  Map.of("Fighting", 2.0, "Poison", 2.0, "Psychic", 0.5, "Dark", 0.0, "Steel", 0.5)),
        Map.entry("Bug",      Map.of("Fire", 0.5, "Grass", 2.0, "Fighting", 0.5, "Flying", 0.5, "Psychic", 2.0, "Ghost", 0.5, "Dark", 2.0, "Steel", 0.5, "Fairy", 0.5)),
        Map.entry("Rock",     Map.of("Fire", 2.0, "Ice", 2.0, "Fighting", 0.5, "Ground", 0.5, "Flying", 2.0, "Bug", 2.0, "Steel", 0.5)),
        Map.entry("Ghost",    Map.of("Normal", 0.0, "Psychic", 2.0, "Ghost", 2.0, "Dark", 0.5)),
        Map.entry("Dragon",   Map.of("Dragon", 2.0, "Steel", 0.5, "Fairy", 0.0)),
        Map.entry("Dark",     Map.of("Fighting", 0.5, "Psychic", 2.0, "Ghost", 2.0, "Dark", 0.5, "Fairy", 0.5)),
        Map.entry("Steel",    Map.of("Fire", 0.5, "Water", 0.5, "Electric", 0.5, "Ice", 2.0, "Rock", 2.0, "Steel", 0.5, "Fairy", 2.0)),
        Map.entry("Fairy",    Map.of("Fire", 0.5, "Fighting", 2.0, "Poison", 0.5, "Dragon", 2.0, "Dark", 2.0, "Steel", 0.5))
    );
}
```

- [ ] **Step 7: Create SeedRunner**

Create `game-data-service/src/main/java/com/pokemon/gamedata/seed/SeedRunner.java`:
```java
package com.pokemon.gamedata.seed;

import com.pokemon.gamedata.client.PokeApiClient;
import com.pokemon.gamedata.client.dto.PokeApiMove;
import com.pokemon.gamedata.client.dto.PokeApiPokemon;
import com.pokemon.gamedata.domain.*;
import com.pokemon.gamedata.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeedRunner implements ApplicationRunner {

    private final PokemonRepository pokemonRepo;
    private final MoveRepository moveRepo;
    private final ItemRepository itemRepo;
    private final FormatRepository formatRepo;
    private final TypeChartRepository typeChartRepo;
    private final PokeApiClient pokeApiClient;

    @Override
    public void run(ApplicationArguments args) {
        if (pokemonRepo.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }
        log.info("Seeding game data from PokeAPI...");
        seedMoves();
        seedPokemon();
        seedItems();
        seedFormats();
        seedTypeChart();
        log.info("Seeding complete: {} pokemon, {} moves, {} items",
            pokemonRepo.count(), moveRepo.count(), itemRepo.count());
    }

    private void seedMoves() {
        for (String slug : SeedData.MOVE_SLUGS) {
            try {
                PokeApiMove apiMove = pokeApiClient.fetchMove(slug);
                Move move = new Move();
                move.setId(slug);
                move.setName(capitalize(apiMove.name().replace("-", " ")));
                move.setType(capitalize(apiMove.type().name()));
                move.setCategory(capitalize(apiMove.damage_class().name()));
                move.setBasePower(apiMove.power());
                move.setAccuracy(apiMove.accuracy());
                move.setPp(apiMove.pp());
                move.setPriority(apiMove.priority());
                moveRepo.save(move);
                log.debug("Seeded move: {}", slug);
            } catch (Exception e) {
                log.error("Failed to seed move {}: {}", slug, e.getMessage());
            }
        }
    }

    private void seedPokemon() {
        for (String slug : SeedData.POKEMON_SLUGS) {
            try {
                PokeApiPokemon apiPokemon = pokeApiClient.fetchPokemon(slug);
                Pokemon p = new Pokemon();
                p.setId(slug);
                p.setDexNumber(apiPokemon.id());
                p.setName(capitalize(apiPokemon.name().replace("-", " ")));
                p.setTypes(apiPokemon.types().stream()
                    .map(t -> capitalize(t.type().name()))
                    .toList());
                p.setBaseStats(new BaseStats(
                    apiPokemon.statValue("hp"),
                    apiPokemon.statValue("attack"),
                    apiPokemon.statValue("defense"),
                    apiPokemon.statValue("special-attack"),
                    apiPokemon.statValue("special-defense"),
                    apiPokemon.statValue("speed")
                ));
                p.setAbilities(apiPokemon.abilities().stream()
                    .filter(a -> !a.is_hidden())
                    .map(a -> capitalize(a.ability().name().replace("-", " ")))
                    .toList());
                p.setLearnset(SeedData.LEARNSETS.getOrDefault(slug, List.of()));
                p.setTier(Map.of("gen9ou", SeedData.GEN9OU_TIERS.getOrDefault(slug, "OU")));
                p.setWeightKg(apiPokemon.weight() / 10.0);
                pokemonRepo.save(p);
                log.debug("Seeded pokemon: {}", slug);
            } catch (Exception e) {
                log.error("Failed to seed pokemon {}: {}", slug, e.getMessage());
            }
        }
    }

    private void seedItems() {
        // Items not in PokeAPI with effect semantics we need — seed hardcoded
        List<Item> items = List.of(
            item("leftovers",         "Leftovers",         "END_OF_TURN_HEAL",      0.0625),
            item("choice-scarf",      "Choice Scarf",      "SPEED_BOOST_LOCK",      1.5),
            item("choice-specs",      "Choice Specs",      "SPA_BOOST_LOCK",        1.5),
            item("choice-band",       "Choice Band",       "ATK_BOOST_LOCK",        1.5),
            item("life-orb",          "Life Orb",          "DAMAGE_BOOST_RECOIL",   1.3),
            item("rocky-helmet",      "Rocky Helmet",      "CONTACT_RECOIL",        0.1667),
            item("heavy-duty-boots",  "Heavy-Duty Boots",  "HAZARD_IMMUNITY",       0.0),
            item("focus-sash",        "Focus Sash",        "SURVIVE_ONE_HIT",       0.0),
            item("assault-vest",      "Assault Vest",      "SPD_BOOST_NO_STATUS",   1.5),
            item("eviolite",          "Eviolite",          "DEF_SPD_BOOST_NFE",     1.5)
        );
        itemRepo.saveAll(items);
    }

    private Item item(String id, String name, String effect, double param) {
        Item i = new Item();
        i.setId(id);
        i.setName(name);
        i.setEffect(effect);
        i.setParam(param);
        return i;
    }

    private void seedFormats() {
        Format ou = new Format();
        ou.setId("gen9ou");
        ou.setName("Gen 9 OU");
        ou.setGeneration(9);
        ou.setBannedPokemon(List.of(
            "flutter-mane", "iron-bundle", "palafin", "annihilape",
            "chien-pao", "ursaluna-bloodmoon"
        ));
        ou.setBannedMoves(List.of("swagger", "baton-pass"));
        ou.setTeamSize(6);
        ou.setActiveSize(1);
        formatRepo.save(ou);
    }

    private void seedTypeChart() {
        TypeChart chart = new TypeChart();
        chart.setId("type_chart_gen9");
        chart.setChart(SeedData.TYPE_CHART);
        typeChartRepo.save(chart);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
```

- [ ] **Step 8: Run all game-data-service tests**

```bash
mvn test -pl game-data-service
```

Expected: All tests pass. (SeedRunner is not tested here — it requires real MongoDB + PokeAPI network access, tested in Task 7 integration test.)

- [ ] **Step 9: Commit**

```bash
git add game-data-service/src/main/java/com/pokemon/gamedata/client/ \
        game-data-service/src/main/java/com/pokemon/gamedata/seed/ \
        game-data-service/src/main/java/com/pokemon/gamedata/config/
git commit -m "feat(game-data): pokeapi client, seed runner, static seed data"
```

---

## Task 6: game-data-service — Services + Controllers

**Files:**
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/service/PokemonService.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/service/ValidationService.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/controller/PokemonController.java`
- Create: `game-data-service/src/main/java/com/pokemon/gamedata/controller/ValidationController.java`
- Test: `game-data-service/src/test/java/com/pokemon/gamedata/service/ValidationServiceTest.java`
- Test: `game-data-service/src/test/java/com/pokemon/gamedata/controller/PokemonControllerTest.java`

- [ ] **Step 1: Write failing controller test**

Create `game-data-service/src/test/java/com/pokemon/gamedata/controller/PokemonControllerTest.java`:
```java
package com.pokemon.gamedata.controller;

import com.pokemon.gamedata.domain.BaseStats;
import com.pokemon.gamedata.domain.Pokemon;
import com.pokemon.gamedata.service.PokemonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PokemonController.class)
class PokemonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PokemonService pokemonService;

    @Test
    void getByIdReturns200WithPokemon() throws Exception {
        Pokemon p = new Pokemon();
        p.setId("garchomp");
        p.setName("Garchomp");
        p.setTypes(List.of("Dragon", "Ground"));
        p.setBaseStats(new BaseStats(108, 130, 95, 80, 85, 102));
        p.setTier(Map.of("gen9ou", "OU"));

        when(pokemonService.findById("garchomp")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/pokemon/garchomp"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("garchomp"))
            .andExpect(jsonPath("$.name").value("Garchomp"))
            .andExpect(jsonPath("$.baseStats.spe").value(102));
    }

    @Test
    void getByIdReturns404WhenNotFound() throws Exception {
        when(pokemonService.findById("fakemon")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pokemon/fakemon"))
            .andExpect(status().isNotFound());
    }

    @Test
    void listAllReturns200() throws Exception {
        when(pokemonService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/pokemon"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

- [ ] **Step 2: Write failing validation service test**

Create `game-data-service/src/test/java/com/pokemon/gamedata/service/ValidationServiceTest.java`:
```java
package com.pokemon.gamedata.service;

import com.pokemon.gamedata.domain.Format;
import com.pokemon.gamedata.domain.Pokemon;
import com.pokemon.gamedata.repository.FormatRepository;
import com.pokemon.gamedata.repository.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock PokemonRepository pokemonRepo;
    @Mock FormatRepository formatRepo;

    @InjectMocks
    ValidationService validationService;

    Format gen9ou;

    @BeforeEach
    void setUp() {
        gen9ou = new Format();
        gen9ou.setId("gen9ou");
        gen9ou.setBannedPokemon(List.of("flutter-mane"));
        gen9ou.setBannedMoves(List.of("swagger"));
        gen9ou.setTeamSize(6);
    }

    @Test
    void validTeamPassesValidation() {
        when(formatRepo.findById("gen9ou")).thenReturn(Optional.of(gen9ou));

        Pokemon garchomp = pokemonWithMoves("garchomp", List.of("earthquake", "swords-dance"));
        when(pokemonRepo.findById("garchomp")).thenReturn(Optional.of(garchomp));

        List<String> teamSlugs = List.of("garchomp", "garchomp", "garchomp", "garchomp", "garchomp", "garchomp");
        ValidationService.ValidationResult result = validationService.validateTeam(teamSlugs, "gen9ou");

        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void teamWithBannedPokemonFailsValidation() {
        when(formatRepo.findById("gen9ou")).thenReturn(Optional.of(gen9ou));

        Pokemon banned = pokemonWithMoves("flutter-mane", List.of("thunderbolt"));
        when(pokemonRepo.findById("flutter-mane")).thenReturn(Optional.of(banned));

        List<String> teamSlugs = List.of("flutter-mane", "flutter-mane", "flutter-mane",
                                          "flutter-mane", "flutter-mane", "flutter-mane");
        ValidationService.ValidationResult result = validationService.validateTeam(teamSlugs, "gen9ou");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("flutter-mane") && e.contains("banned"));
    }

    @Test
    void teamWrongSizeFailsValidation() {
        when(formatRepo.findById("gen9ou")).thenReturn(Optional.of(gen9ou));

        List<String> teamSlugs = List.of("garchomp", "garchomp");  // only 2 instead of 6
        ValidationService.ValidationResult result = validationService.validateTeam(teamSlugs, "gen9ou");

        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).anyMatch(e -> e.contains("size"));
    }

    private Pokemon pokemonWithMoves(String id, List<String> moves) {
        Pokemon p = new Pokemon();
        p.setId(id);
        p.setLearnset(moves);
        p.setTier(Map.of("gen9ou", "OU"));
        return p;
    }
}
```

- [ ] **Step 3: Run tests — expect compilation failure**

```bash
mvn test -pl game-data-service -Dtest="PokemonControllerTest,ValidationServiceTest" 2>&1 | tail -5
```

Expected: compilation error — `PokemonService`, `ValidationService`, `PokemonController` not yet written.

- [ ] **Step 4: Write PokemonService**

Create `game-data-service/src/main/java/com/pokemon/gamedata/service/PokemonService.java`:
```java
package com.pokemon.gamedata.service;

import com.pokemon.gamedata.domain.Pokemon;
import com.pokemon.gamedata.repository.PokemonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PokemonService {

    private final PokemonRepository repository;

    public List<Pokemon> findAll() {
        return repository.findAll();
    }

    public Optional<Pokemon> findById(String id) {
        return repository.findById(id);
    }

    public List<Pokemon> findByGen9ouTier(String tier) {
        return repository.findByTierGen9ou(tier);
    }
}
```

- [ ] **Step 5: Write ValidationService**

Create `game-data-service/src/main/java/com/pokemon/gamedata/service/ValidationService.java`:
```java
package com.pokemon.gamedata.service;

import com.pokemon.gamedata.domain.Format;
import com.pokemon.gamedata.repository.FormatRepository;
import com.pokemon.gamedata.repository.PokemonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final PokemonRepository pokemonRepo;
    private final FormatRepository formatRepo;

    public ValidationResult validateTeam(List<String> pokemonSlugs, String formatId) {
        List<String> errors = new ArrayList<>();

        Format format = formatRepo.findById(formatId)
            .orElseThrow(() -> new IllegalArgumentException("Unknown format: " + formatId));

        if (pokemonSlugs.size() != format.getTeamSize()) {
            errors.add("Team size must be " + format.getTeamSize() + ", got " + pokemonSlugs.size());
        }

        for (String slug : pokemonSlugs) {
            if (format.getBannedPokemon().contains(slug)) {
                errors.add(slug + " is banned in " + formatId);
            }
            if (pokemonRepo.findById(slug).isEmpty()) {
                errors.add(slug + " is not a known Pokemon");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    public record ValidationResult(boolean valid, List<String> errors) {}
}
```

- [ ] **Step 6: Write PokemonController**

Create `game-data-service/src/main/java/com/pokemon/gamedata/controller/PokemonController.java`:
```java
package com.pokemon.gamedata.controller;

import com.pokemon.gamedata.domain.Pokemon;
import com.pokemon.gamedata.service.PokemonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
@RequiredArgsConstructor
public class PokemonController {

    private final PokemonService pokemonService;

    @GetMapping
    public List<Pokemon> listAll() {
        return pokemonService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokemon> getById(@PathVariable String id) {
        return pokemonService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tier/{tier}")
    public List<Pokemon> getByTier(@PathVariable String tier) {
        return pokemonService.findByGen9ouTier(tier.toUpperCase());
    }
}
```

Create `game-data-service/src/main/java/com/pokemon/gamedata/controller/ValidationController.java`:
```java
package com.pokemon.gamedata.controller;

import com.pokemon.gamedata.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/validate")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/team")
    public ValidationService.ValidationResult validateTeam(@RequestBody TeamValidationRequest req) {
        return validationService.validateTeam(req.pokemonSlugs(), req.formatId());
    }

    public record TeamValidationRequest(List<String> pokemonSlugs, String formatId) {}
}
```

- [ ] **Step 7: Run tests — expect GREEN**

```bash
mvn test -pl game-data-service
```

Expected:
```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- [ ] **Step 8: Commit**

```bash
git add game-data-service/src/main/java/com/pokemon/gamedata/service/ \
        game-data-service/src/main/java/com/pokemon/gamedata/controller/
git commit -m "feat(game-data): services, controllers, validation"
```

---

## Task 7: End-to-end smoke test of game-data-service

- [ ] **Step 1: Start game-data-service against running Docker Compose**

Ensure infra is running:
```bash
cd infra && docker compose up -d && cd ..
```

Start game-data-service:
```bash
mvn spring-boot:run -pl game-data-service
```

Wait for log line: `Seeding complete: 20 pokemon, 20 moves, 10 items`

- [ ] **Step 2: Verify seed data via curl**

```bash
curl -s http://localhost:8082/api/pokemon/garchomp | python3 -m json.tool
```

Expected (truncated):
```json
{
  "id": "garchomp",
  "name": "Garchomp",
  "types": ["Dragon", "Ground"],
  "baseStats": { "hp": 108, "atk": 130, "def": 95, "spa": 80, "spd": 85, "spe": 102 },
  "tier": { "gen9ou": "OU" }
}
```

```bash
curl -s http://localhost:8082/api/pokemon/fakemon
```

Expected: `404` status.

```bash
curl -s http://localhost:8082/api/pokemon/tier/OU | python3 -m json.tool | grep '"id"'
```

Expected: 20 `"id"` lines (all seeded OU Pokemon).

- [ ] **Step 3: Validate team endpoint**

```bash
curl -s -X POST http://localhost:8082/api/validate/team \
  -H 'Content-Type: application/json' \
  -d '{
    "pokemonSlugs": ["garchomp","great-tusk","gholdengo","kingambit","dragapult","volcarona"],
    "formatId": "gen9ou"
  }' | python3 -m json.tool
```

Expected:
```json
{ "valid": true, "errors": [] }
```

- [ ] **Step 4: Stop service, commit**

Stop with Ctrl+C.

```bash
git add .
git commit -m "test: e2e smoke test passed, game-data-service fully operational"
```

---

## Open Questions

- PokeAPI rate limits? Seed is ~40 requests — fine for once, but add retry/backoff if flaky CI
- gen9 learnsets hardcoded — add more moves as battle-service needs them in Plan 3
- Item effects defined as string enum (`END_OF_TURN_HEAL`) — battle-service must implement matching handler per effect key
- `findByTierGen9ou` uses hardcoded `gen9ou` — revisit if multi-gen added later
