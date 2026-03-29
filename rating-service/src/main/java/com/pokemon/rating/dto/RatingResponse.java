package com.pokemon.rating.dto;

import java.util.UUID;

public record RatingResponse(UUID userId, String username, String format, int elo, int wins, int losses) {}
