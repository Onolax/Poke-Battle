package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.BaseStats;
import com.pokemon.gamedata.domain.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class PokemonRepositoryTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    PokemonRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void savesAndFindsById() {
        Pokemon p = new Pokemon();
        p.setId("pikachu");
        p.setDexNumber(25);
        p.setName("Pikachu");
        p.setTypes(List.of("Electric"));
        p.setBaseStats(new BaseStats(35, 55, 40, 50, 50, 90));
        p.setAbilities(List.of("Static", "Lightning Rod"));
        p.setLearnset(List.of("thunderbolt", "volt-tackle"));
        p.setTier(Map.of("gen9ou", "NU"));
        p.setWeightKg(6.0);

        repository.save(p);

        Optional<Pokemon> found = repository.findById("pikachu");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Pikachu");
        assertThat(found.get().getBaseStats().getSpe()).isEqualTo(90);
        assertThat(found.get().getTier()).containsEntry("gen9ou", "NU");
    }

    @Test
    void findsByTier() {
        Pokemon ou = new Pokemon();
        ou.setId("garchomp");
        ou.setName("Garchomp");
        ou.setTier(Map.of("gen9ou", "OU"));
        repository.save(ou);

        Pokemon nu = new Pokemon();
        nu.setId("pikachu");
        nu.setName("Pikachu");
        nu.setTier(Map.of("gen9ou", "NU"));
        repository.save(nu);

        List<Pokemon> ouPokemon = repository.findByTierGen9ou("OU");
        assertThat(ouPokemon).hasSize(1);
        assertThat(ouPokemon.get(0).getId()).isEqualTo("garchomp");
    }
}
