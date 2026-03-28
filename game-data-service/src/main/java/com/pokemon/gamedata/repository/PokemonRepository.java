package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Pokemon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PokemonRepository extends MongoRepository<Pokemon, String> {
    @Query("{ 'tier.gen9ou': ?0 }")
    List<Pokemon> findByTierGen9ou(String tier);
}
