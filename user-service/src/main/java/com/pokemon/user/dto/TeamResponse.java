package com.pokemon.user.dto;

import java.util.List;
import java.util.UUID;

public record TeamResponse(UUID id, String name, String format, List<String> pokemonSlugs, boolean validated) {}
