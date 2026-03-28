package com.pokemon.gamedata.service;

import com.pokemon.gamedata.domain.Pokemon;
import com.pokemon.gamedata.repository.PokemonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PokemonService {

    private final PokemonRepository pokemonRepository;

    public PokemonService(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    public List<Pokemon> findAll() {
        return pokemonRepository.findAll();
    }

    public Optional<Pokemon> findById(String id) {
        return pokemonRepository.findById(id);
    }

    public List<Pokemon> findByGen9ouTier(String tier) {
        return pokemonRepository.findByTierGen9ou(tier);
    }
}
