package com.pokemon.gamedata.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PokeApiPokemon(
        int id,
        String name,
        List<StatEntry> stats,
        List<TypeEntry> types,
        List<AbilityEntry> abilities,
        int weight
) {
    public record StatEntry(@JsonProperty("base_stat") int baseStat, StatName stat) {}
    public record StatName(String name) {}
    public record TypeEntry(TypeName type) {}
    public record TypeName(String name) {}
    public record AbilityEntry(AbilityName ability, @JsonProperty("is_hidden") boolean isHidden) {}
    public record AbilityName(String name) {}
}
