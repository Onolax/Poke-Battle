package com.pokemon.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.user.dto.CreateTeamRequest;
import com.pokemon.user.dto.CreateTeamResponse;
import com.pokemon.user.dto.TeamResponse;
import com.pokemon.user.exception.GlobalExceptionHandler;
import com.pokemon.user.exception.TeamNotFoundException;
import com.pokemon.user.service.TeamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@Import(GlobalExceptionHandler.class)
class TeamControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TeamService teamService;

    static final UUID USER_ID = UUID.randomUUID();
    static final UUID TEAM_ID = UUID.randomUUID();

    @Test
    void createTeam_returns201() throws Exception {
        when(teamService.createTeam(eq(USER_ID), any())).thenReturn(new CreateTeamResponse(TEAM_ID));

        mockMvc.perform(post("/api/teams")
                .header("X-User-Id", USER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateTeamRequest("My Team", "GEN9OU",
                                List.of("garchomp", "landorus-therian", "volcarona",
                                        "iron-valiant", "great-tusk", "kingambit")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.teamId").value(TEAM_ID.toString()));
    }

    @Test
    void getTeams_returns200() throws Exception {
        when(teamService.getTeams(USER_ID)).thenReturn(List.of(
                new TeamResponse(TEAM_ID, "My Team", "GEN9OU", List.of("garchomp"), true)));

        mockMvc.perform(get("/api/teams")
                .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Team"));
    }

    @Test
    void getTeam_returns200() throws Exception {
        when(teamService.getTeam(USER_ID, TEAM_ID)).thenReturn(
                new TeamResponse(TEAM_ID, "My Team", "GEN9OU", List.of("garchomp"), true));

        mockMvc.perform(get("/api/teams/{id}", TEAM_ID)
                .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEAM_ID.toString()));
    }

    @Test
    void getTeam_notFound_returns404() throws Exception {
        when(teamService.getTeam(any(), any())).thenThrow(new TeamNotFoundException(TEAM_ID.toString()));

        mockMvc.perform(get("/api/teams/{id}", TEAM_ID)
                .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTeam_returns204() throws Exception {
        doNothing().when(teamService).deleteTeam(USER_ID, TEAM_ID);

        mockMvc.perform(delete("/api/teams/{id}", TEAM_ID)
                .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isNoContent());
    }
}
