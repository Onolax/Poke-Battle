package com.pokemon.lobby.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ZSetOperations<String, String> zSetOps;
    @Mock ValueOperations<String, String> valueOps;

    LobbyService lobbyService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        lobbyService = new LobbyService(redisTemplate);
    }

    @Test
    void enqueue_addsUserToSortedSet() {
        lobbyService.enqueue("user1", "team1", "GEN9OU");
        verify(zSetOps).add(eq("queue:GEN9OU"), eq("user1:team1"), anyDouble());
    }

    @Test
    void dequeue_removesUserFromSortedSet() {
        when(zSetOps.range("queue:GEN9OU", 0, -1)).thenReturn(Set.of("user1:team1", "user2:team2"));
        lobbyService.dequeue("user1", "GEN9OU");
        verify(zSetOps).remove("queue:GEN9OU", "user1:team1");
    }

    @Test
    void getStatus_returnsMatched_whenBattleIdExists() {
        when(valueOps.get("lobby:user:user1:battleId")).thenReturn("battle-123");
        var status = lobbyService.getStatus("user1");
        assertThat(status.status()).isEqualTo("MATCHED");
        assertThat(status.battleId()).isEqualTo("battle-123");
    }

    @Test
    void getStatus_returnsWaiting_whenNoBattleId() {
        when(valueOps.get("lobby:user:user1:battleId")).thenReturn(null);
        var status = lobbyService.getStatus("user1");
        assertThat(status.status()).isEqualTo("WAITING");
        assertThat(status.battleId()).isNull();
    }

    @Test
    void storeBattleId_setsKeyWithTtl() {
        lobbyService.storeBattleId("user1", "battle-abc");
        verify(valueOps).set(eq("lobby:user:user1:battleId"), eq("battle-abc"), any());
    }
}
