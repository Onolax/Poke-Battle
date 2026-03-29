package com.pokemon.lobby.service;

import com.pokemon.lobby.dto.QueueStatusResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class LobbyService {

    static final String QUEUE_KEY_PREFIX = "queue:";
    static final String USER_BATTLE_KEY_PREFIX = "lobby:user:";
    static final String BATTLE_KEY_SUFFIX = ":battleId";

    private final RedisTemplate<String, String> redisTemplate;

    public LobbyService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueue(String userId, String teamId, String format) {
        String queueKey = QUEUE_KEY_PREFIX + format;
        String member = userId + ":" + teamId;
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(queueKey, member, score);
    }

    public void dequeue(String userId, String format) {
        String queueKey = QUEUE_KEY_PREFIX + format;
        // Remove any entry for this user (member starts with userId:)
        var members = redisTemplate.opsForZSet().range(queueKey, 0, -1);
        if (members != null) {
            members.stream()
                    .filter(m -> m.startsWith(userId + ":"))
                    .forEach(m -> redisTemplate.opsForZSet().remove(queueKey, m));
        }
    }

    public QueueStatusResponse getStatus(String userId) {
        String key = USER_BATTLE_KEY_PREFIX + userId + BATTLE_KEY_SUFFIX;
        String battleId = redisTemplate.opsForValue().get(key);
        if (battleId != null) {
            return QueueStatusResponse.matched(battleId);
        }
        return QueueStatusResponse.waiting();
    }

    public void storeBattleId(String userId, String battleId) {
        String key = USER_BATTLE_KEY_PREFIX + userId + BATTLE_KEY_SUFFIX;
        redisTemplate.opsForValue().set(key, battleId, Duration.ofSeconds(600));
    }
}
