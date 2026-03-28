package com.pokemon.user.controller;

import com.pokemon.user.dto.CreateTeamRequest;
import com.pokemon.user.dto.CreateTeamResponse;
import com.pokemon.user.dto.TeamResponse;
import com.pokemon.user.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTeamResponse createTeam(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateTeamRequest req) {
        return teamService.createTeam(userId, req);
    }

    @GetMapping
    public List<TeamResponse> getTeams(@RequestHeader("X-User-Id") UUID userId) {
        return teamService.getTeams(userId);
    }

    @GetMapping("/{id}")
    public TeamResponse getTeam(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return teamService.getTeam(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        teamService.deleteTeam(userId, id);
    }
}
