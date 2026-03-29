package com.pokemon.battle.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.battle.dto.*;
import com.pokemon.battle.redis.BattleStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class MatchFoundConsumer {

    private static final Logger log = LoggerFactory.getLogger(MatchFoundConsumer.class);

    private final BattleStateRepository repo;
    private final BattleEventProducer producer;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MatchFoundConsumer(BattleStateRepository repo, BattleEventProducer producer,
                               RestClient.Builder restClientBuilder, ObjectMapper objectMapper,
                               @Value("${USER_SERVICE_URL:http://user-service:8081}") String userServiceUrl) {
        this.repo = repo;
        this.producer = producer;
        this.restClient = restClientBuilder.baseUrl(userServiceUrl).build();
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "match.found", groupId = "battle-service")
    public void onMatchFound(MatchFoundEvent event) {
        log.info("Match found: {} vs {} → battle {}", event.player1Id(), event.player2Id(), event.battleId());

        // Fetch teams from user-service (internal bypass — no JWT needed)
        BattlePokemon[] team1 = fetchTeam(event.player1TeamId());
        BattlePokemon[] team2 = fetchTeam(event.player2TeamId());

        if (team1 == null || team2 == null) {
            log.error("Failed to fetch teams for battle {}", event.battleId());
            return;
        }

        // Initialise battle state
        BattleState state = new BattleState();
        state.battleId = event.battleId();
        state.phase = "ACTIVE";
        state.turnNumber = 0;
        state.player1Id = event.player1Id();
        state.player2Id = event.player2Id();
        state.activeSlot1 = 0;
        state.activeSlot2 = 0;

        repo.saveState(state);
        repo.saveTeam(event.battleId(), "p1", team1);
        repo.saveTeam(event.battleId(), "p2", team2);

        producer.sendBattleStarted(new BattleStartedEvent(event.battleId(), event.player1Id(), event.player2Id()));
        log.info("Battle {} initialised", event.battleId());
    }

    private BattlePokemon[] fetchTeam(String teamId) {
        try {
            String json = restClient.get()
                    .uri("/api/teams/" + teamId)
                    .header("X-Internal-Call", "true")
                    .retrieve()
                    .body(String.class);

            JsonNode teamNode = objectMapper.readTree(json);
            JsonNode slugsNode = teamNode.get("pokemonSlugs");
            if (slugsNode == null) return null;

            List<BattlePokemon> team = new ArrayList<>();
            for (JsonNode slug : slugsNode) {
                BattlePokemon bp = buildBattlePokemon(slug.asText());
                if (bp != null) team.add(bp);
            }
            return team.toArray(new BattlePokemon[0]);
        } catch (Exception e) {
            log.error("Failed to fetch team {}: {}", teamId, e.getMessage());
            return null;
        }
    }

    private BattlePokemon buildBattlePokemon(String slug) {
        // In a full implementation we'd call game-data-service for move/stat data.
        // Here we build a basic battle pokemon with default stats and moves.
        BattlePokemon bp = new BattlePokemon();
        bp.slug = slug;
        bp.name = toDisplayName(slug);
        bp.types = List.of("Normal"); // game-data-service would provide real types
        bp.maxHp = 200;
        bp.currentHp = 200;
        bp.attack = 80;
        bp.defense = 70;
        bp.spAtk = 75;
        bp.spDef = 70;
        bp.speed = 85;
        bp.fainted = false;

        // Default moves
        BattleMove tackle = new BattleMove();
        tackle.slug = "tackle"; tackle.name = "Tackle"; tackle.type = "Normal";
        tackle.category = "Physical"; tackle.basePower = 40; tackle.accuracy = 100;
        tackle.pp = 35; tackle.currentPp = 35; tackle.priority = 0;

        BattleMove growl = new BattleMove();
        growl.slug = "growl"; growl.name = "Growl"; growl.type = "Normal";
        growl.category = "Status"; growl.basePower = 0; growl.accuracy = 100;
        growl.pp = 40; growl.currentPp = 40; growl.priority = 0;

        bp.moves = List.of(tackle, growl);
        return bp;
    }

    private String toDisplayName(String slug) {
        if (slug == null || slug.isEmpty()) return slug;
        return Character.toUpperCase(slug.charAt(0)) + slug.substring(1).replace("-", " ");
    }
}
