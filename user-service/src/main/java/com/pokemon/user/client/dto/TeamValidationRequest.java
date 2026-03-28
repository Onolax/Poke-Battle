package com.pokemon.user.client.dto;

import java.util.List;

public record TeamValidationRequest(List<String> pokemonSlugs, String formatId) {}
