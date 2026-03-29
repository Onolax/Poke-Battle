package com.pokemon.battle.dto;

public record BattleStartedEvent(String battleId, String player1Id, String player2Id) {}
