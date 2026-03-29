package com.pokemon.rating.dto;

public record BattleEndedEvent(
        String battleId,
        String winnerId,
        String winnerUsername,
        String loserId,
        String loserUsername,
        String format,
        int turns
) {}
