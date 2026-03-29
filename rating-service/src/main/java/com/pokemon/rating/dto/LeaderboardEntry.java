package com.pokemon.rating.dto;

import java.util.UUID;

public record LeaderboardEntry(int rank, UUID userId, String username, int elo, int wins, int losses) {}
