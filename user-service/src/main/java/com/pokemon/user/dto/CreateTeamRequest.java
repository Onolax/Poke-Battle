package com.pokemon.user.dto;

import java.util.List;

public record CreateTeamRequest(String name, String format, List<String> pokemonSlugs) {}
