package com.pokemon.battle.dto;

public record MoveAction(String type, String value) {
    public static MoveAction move(String moveSlug) { return new MoveAction("MOVE", moveSlug); }
    public static MoveAction switchPokemon(String slot) { return new MoveAction("SWITCH", slot); }
    public static MoveAction forfeit() { return new MoveAction("FORFEIT", null); }
}
