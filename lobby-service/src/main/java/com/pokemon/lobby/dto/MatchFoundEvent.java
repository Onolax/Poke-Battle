package com.pokemon.lobby.dto;

public record MatchFoundEvent(
        String battleId,
        String player1Id,
        String player1TeamId,
        String player2Id,
        String player2TeamId,
        String format
) {}
