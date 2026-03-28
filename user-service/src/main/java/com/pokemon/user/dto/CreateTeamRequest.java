package com.pokemon.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateTeamRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank String format,
        @NotEmpty @Size(min = 1, max = 6) List<String> pokemonSlugs
) {}
