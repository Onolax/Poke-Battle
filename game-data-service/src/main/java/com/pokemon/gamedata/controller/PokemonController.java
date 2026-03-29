package com.pokemon.gamedata.controller;

import com.pokemon.gamedata.domain.Pokemon;
import com.pokemon.gamedata.service.PokemonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping
    public List<Pokemon> listAll() {
        return pokemonService.findAll();
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Pokemon> getByName(@PathVariable String name) {
        return pokemonService.findByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokemon> getById(@PathVariable String id) {
        return pokemonService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tier/{tier}")
    public List<Pokemon> getByTier(@PathVariable String tier) {
        return pokemonService.findByGen9ouTier(tier);
    }
}
