package com.pokemon.gamedata.service;

import com.pokemon.gamedata.domain.Format;
import com.pokemon.gamedata.repository.FormatRepository;
import com.pokemon.gamedata.repository.PokemonRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValidationService {

    private final PokemonRepository pokemonRepository;
    private final FormatRepository formatRepository;

    public ValidationService(PokemonRepository pokemonRepository, FormatRepository formatRepository) {
        this.pokemonRepository = pokemonRepository;
        this.formatRepository = formatRepository;
    }

    public ValidationResult validateTeam(List<String> slugs, String formatId) {
        Format format = formatRepository.findById(formatId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown format: " + formatId));

        List<String> errors = new ArrayList<>();

        if (slugs.size() != format.getTeamSize()) {
            errors.add("Team must have " + format.getTeamSize() + " pokemon, got " + slugs.size());
        }

        for (String slug : slugs) {
            if (format.getBannedPokemon().contains(slug)) {
                errors.add(slug + " is banned in " + format.getName());
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
