package com.pokemon.lobby.service;

import com.pokemon.lobby.dto.MatchFoundEvent;
import com.pokemon.lobby.kafka.MatchFoundProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchmakerSchedulerTest {

    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock LobbyService lobbyService;
    @Mock MatchFoundProducer producer;

    @Test
    void matchPlayers_pairsTwo_andPublishesEvent() {
        when(redisTemplate.execute(any(RedisScript.class), any(List.class)))
                .thenReturn(List.of("player1:team-a", "player2:team-b"));

        var scheduler = new MatchmakerScheduler(redisTemplate, lobbyService, producer);
        scheduler.matchPlayers();

        ArgumentCaptor<MatchFoundEvent> captor = ArgumentCaptor.forClass(MatchFoundEvent.class);
        verify(producer).send(captor.capture());
        MatchFoundEvent event = captor.getValue();

        assertThat(event.player1Id()).isEqualTo("player1");
        assertThat(event.player1TeamId()).isEqualTo("team-a");
        assertThat(event.player2Id()).isEqualTo("player2");
        assertThat(event.player2TeamId()).isEqualTo("team-b");
        assertThat(event.format()).isEqualTo("GEN9OU");
        assertThat(event.battleId()).isNotBlank();

        verify(lobbyService).storeBattleId(eq("player1"), eq(event.battleId()));
        verify(lobbyService).storeBattleId(eq("player2"), eq(event.battleId()));
    }

    @Test
    void matchPlayers_doesNothing_whenFewerThanTwo() {
        when(redisTemplate.execute(any(RedisScript.class), any(List.class)))
                .thenReturn(List.of());

        var scheduler = new MatchmakerScheduler(redisTemplate, lobbyService, producer);
        scheduler.matchPlayers();

        verifyNoInteractions(producer, lobbyService);
    }
}
