package com.pokemon.gamedata.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PokeApiMove(
        String name,
        Integer power,
        int pp,
        Integer accuracy,
        int priority,
        @JsonProperty("damage_class") DamageClass damageClass,
        TypeName type
) {
    public record DamageClass(String name) {}
    public record TypeName(String name) {}
}
