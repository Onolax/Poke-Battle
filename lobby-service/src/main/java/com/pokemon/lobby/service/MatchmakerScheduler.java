package com.pokemon.lobby.service;

import com.pokemon.lobby.dto.MatchFoundEvent;
import com.pokemon.lobby.kafka.MatchFoundProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MatchmakerScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchmakerScheduler.class);
    private static final String FORMAT = "GEN9OU";
    private static final String QUEUE_KEY = LobbyService.QUEUE_KEY_PREFIX + FORMAT;

    // Atomically peek top 2 members, remove them if 2 exist, return them
    private static final DefaultRedisScript<List> PAIR_SCRIPT = new DefaultRedisScript<>("""
            local members = redis.call('ZRANGE', KEYS[1], 0, 1)
            if #members < 2 then return {} end
            redis.call('ZREM', KEYS[1], members[1], members[2])
            return members
            """, List.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final LobbyService lobbyService;
    private final MatchFoundProducer producer;

    public MatchmakerScheduler(RedisTemplate<String, String> redisTemplate,
                                LobbyService lobbyService,
                                MatchFoundProducer producer) {
        this.redisTemplate = redisTemplate;
        this.lobbyService = lobbyService;
        this.producer = producer;
    }

    @Scheduled(fixedDelay = 1000)
    public void matchPlayers() {
        @SuppressWarnings("unchecked")
        List<String> paired = redisTemplate.execute(PAIR_SCRIPT, List.of(QUEUE_KEY));
        if (paired == null || paired.size() < 2) return;

        String[] p1 = paired.get(0).split(":", 2);
        String[] p2 = paired.get(1).split(":", 2);
        String battleId = UUID.randomUUID().toString();

        lobbyService.storeBattleId(p1[0], battleId);
        lobbyService.storeBattleId(p2[0], battleId);

        producer.send(new MatchFoundEvent(battleId, p1[0], p1[1], p2[0], p2[1], FORMAT));
        log.info("Matched {} vs {} → battle {}", p1[0], p2[0], battleId);
    }
}
