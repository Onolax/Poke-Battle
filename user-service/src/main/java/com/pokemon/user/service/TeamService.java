package com.pokemon.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.user.client.GameDataClient;
import com.pokemon.user.client.dto.TeamValidationResult;
import com.pokemon.user.domain.Team;
import com.pokemon.user.domain.User;
import com.pokemon.user.dto.CreateTeamRequest;
import com.pokemon.user.dto.CreateTeamResponse;
import com.pokemon.user.dto.TeamResponse;
import com.pokemon.user.exception.TeamNotFoundException;
import com.pokemon.user.exception.TeamValidationException;
import com.pokemon.user.exception.UserNotFoundException;
import com.pokemon.user.repository.TeamRepository;
import com.pokemon.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final GameDataClient gameDataClient;
    private final ObjectMapper objectMapper;

    public TeamService(TeamRepository teamRepository, UserRepository userRepository,
                       GameDataClient gameDataClient, ObjectMapper objectMapper) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.gameDataClient = gameDataClient;
        this.objectMapper = objectMapper;
    }

    public CreateTeamResponse createTeam(UUID userId, CreateTeamRequest req) {
        TeamValidationResult validation = gameDataClient.validateTeam(req.pokemonSlugs(), req.format());
        if (!validation.valid()) {
            throw new TeamValidationException(validation.errors());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));

        Team team = new Team();
        team.setUser(user);
        team.setName(req.name());
        team.setFormat(req.format());
        team.setPokemonData(serialize(req.pokemonSlugs()));
        team.setValidated(true);

        Team saved = teamRepository.save(team);
        return new CreateTeamResponse(saved.getId());
    }

    public List<TeamResponse> getTeams(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        return teamRepository.findAllByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public TeamResponse getTeam(UUID userId, UUID teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamRepository.findByIdAndUser(teamId, user)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        return toResponse(team);
    }

    public void deleteTeam(UUID userId, UUID teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamRepository.findByIdAndUser(teamId, user)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        teamRepository.delete(team);
    }

    private TeamResponse toResponse(Team team) {
        return new TeamResponse(team.getId(), team.getName(), team.getFormat(),
                deserialize(team.getPokemonData()), team.isValidated());
    }

    private String serialize(List<String> slugs) {
        try {
            return objectMapper.writeValueAsString(slugs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize pokemon slugs", e);
        }
    }

    private List<String> deserialize(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
