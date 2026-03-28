package com.pokemon.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.user.client.GameDataClient;
import com.pokemon.user.client.dto.TeamValidationResult;
import com.pokemon.user.domain.Team;
import com.pokemon.user.domain.User;
import com.pokemon.user.dto.CreateTeamRequest;
import com.pokemon.user.dto.CreateTeamResponse;
import com.pokemon.user.exception.TeamNotFoundException;
import com.pokemon.user.exception.TeamValidationException;
import com.pokemon.user.repository.TeamRepository;
import com.pokemon.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock TeamRepository teamRepository;
    @Mock UserRepository userRepository;
    @Mock GameDataClient gameDataClient;
    @Spy ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks TeamService teamService;

    UUID userId = UUID.randomUUID();
    UUID teamId = UUID.randomUUID();

    @Test
    void createTeam_valid_savesAndReturnsId() throws Exception {
        User user = new User(); user.setId(userId); user.setUsername("ash");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(gameDataClient.validateTeam(any(), any()))
                .thenReturn(new TeamValidationResult(true, List.of()));

        Team saved = new Team(); saved.setId(teamId);
        when(teamRepository.save(any())).thenReturn(saved);

        CreateTeamRequest req = new CreateTeamRequest("My Team", "GEN9OU",
                List.of("garchomp", "landorus-therian", "volcarona", "iron-valiant", "great-tusk", "kingambit"));
        CreateTeamResponse resp = teamService.createTeam(userId, req);

        assertThat(resp.teamId()).isEqualTo(teamId);
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void createTeam_invalid_throwsValidationException() {
        when(gameDataClient.validateTeam(any(), any()))
                .thenReturn(new TeamValidationResult(false, List.of("flutter-mane is banned")));

        assertThatThrownBy(() -> teamService.createTeam(userId,
                new CreateTeamRequest("Bad Team", "GEN9OU", List.of("flutter-mane"))))
                .isInstanceOf(TeamValidationException.class);
        verify(teamRepository, never()).save(any());
    }

    @Test
    void getTeam_wrongOwner_throwsNotFoundException() {
        User user = new User(); user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teamRepository.findByIdAndUser(teamId, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeam(userId, teamId))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void deleteTeam_ownerCanDelete() {
        User user = new User(); user.setId(userId);
        Team team = new Team(); team.setId(teamId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teamRepository.findByIdAndUser(teamId, user)).thenReturn(Optional.of(team));

        teamService.deleteTeam(userId, teamId);
        verify(teamRepository).delete(team);
    }
}
