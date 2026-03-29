package com.pokemon.battle.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.battle.dto.BattlePokemon;
import com.pokemon.battle.dto.BattleState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Repository
public class BattleStateRepository {

    private static final String STATE_KEY = "battle:%s:state";
    private static final String TEAM_P1_KEY = "battle:%s:team:p1";
    private static final String TEAM_P2_KEY = "battle:%s:team:p2";
    private static final String PENDING_KEY = "battle:%s:moves:pending";
    private static final String LOCK_KEY = "battle:%s:lock";
    private static final Duration ACTIVE_TTL = Duration.ofHours(2);
    private static final Duration ENDED_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public BattleStateRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void saveState(BattleState state) {
        String key = STATE_KEY.formatted(state.battleId);
        redisTemplate.opsForHash().put(key, "battleId", state.battleId);
        redisTemplate.opsForHash().put(key, "phase", state.phase);
        redisTemplate.opsForHash().put(key, "turnNumber", String.valueOf(state.turnNumber));
        redisTemplate.opsForHash().put(key, "player1Id", state.player1Id);
        redisTemplate.opsForHash().put(key, "player1Username", nvl(state.player1Username));
        redisTemplate.opsForHash().put(key, "player2Id", state.player2Id);
        redisTemplate.opsForHash().put(key, "player2Username", nvl(state.player2Username));
        redisTemplate.opsForHash().put(key, "activeSlot1", String.valueOf(state.activeSlot1));
        redisTemplate.opsForHash().put(key, "activeSlot2", String.valueOf(state.activeSlot2));
        redisTemplate.opsForHash().put(key, "weather", nvl(state.weather));
        redisTemplate.opsForHash().put(key, "winnerId", nvl(state.winnerId));
        redisTemplate.expire(key, ACTIVE_TTL);
    }

    public BattleState loadState(String battleId) {
        String key = STATE_KEY.formatted(battleId);
        var entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) return null;
        BattleState s = new BattleState();
        s.battleId = str(entries.get("battleId"));
        s.phase = str(entries.get("phase"));
        s.turnNumber = parseInt(entries.get("turnNumber"));
        s.player1Id = str(entries.get("player1Id"));
        s.player1Username = str(entries.get("player1Username"));
        s.player2Id = str(entries.get("player2Id"));
        s.player2Username = str(entries.get("player2Username"));
        s.activeSlot1 = parseInt(entries.get("activeSlot1"));
        s.activeSlot2 = parseInt(entries.get("activeSlot2"));
        s.weather = str(entries.get("weather"));
        s.winnerId = str(entries.get("winnerId"));
        return s;
    }

    public void saveTeam(String battleId, String side, BattlePokemon[] team) {
        String key = ("p1".equals(side) ? TEAM_P1_KEY : TEAM_P2_KEY).formatted(battleId);
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(team));
            redisTemplate.expire(key, ACTIVE_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public BattlePokemon[] loadTeam(String battleId, String side) {
        String key = ("p1".equals(side) ? TEAM_P1_KEY : TEAM_P2_KEY).formatted(battleId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, BattlePokemon[].class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean submitMove(String battleId, String playerKey, String action) {
        String key = PENDING_KEY.formatted(battleId);
        Boolean added = redisTemplate.opsForHash().putIfAbsent(key, playerKey, action);
        redisTemplate.expire(key, Duration.ofMinutes(5));
        return Boolean.TRUE.equals(added);
    }

    public java.util.Map<Object, Object> getPendingMoves(String battleId) {
        return redisTemplate.opsForHash().entries(PENDING_KEY.formatted(battleId));
    }

    public void clearPendingMoves(String battleId) {
        redisTemplate.delete(PENDING_KEY.formatted(battleId));
    }

    public boolean acquireLock(String battleId, String podId) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY.formatted(battleId), podId, Duration.ofSeconds(5));
        return Boolean.TRUE.equals(result);
    }

    public void releaseLock(String battleId) {
        redisTemplate.delete(LOCK_KEY.formatted(battleId));
    }

    public void publish(String battleId, String message) {
        redisTemplate.convertAndSend("battle:" + battleId + ":events", message);
    }

    public void setEndedTtl(String battleId) {
        for (String pattern : new String[]{STATE_KEY, TEAM_P1_KEY, TEAM_P2_KEY, PENDING_KEY}) {
            redisTemplate.expire(pattern.formatted(battleId), ENDED_TTL);
        }
    }

    private String str(Object o) { return o == null ? null : o.toString(); }
    private int parseInt(Object o) { return o == null ? 0 : Integer.parseInt(o.toString()); }
    private String nvl(String s) { return s == null ? "" : s; }
}
