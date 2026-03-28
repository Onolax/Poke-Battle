package com.pokemon.gamedata.service;

import com.pokemon.gamedata.domain.Format;
import com.pokemon.gamedata.repository.FormatRepository;
import com.pokemon.gamedata.repository.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    PokemonRepository pokemonRepository;

    @Mock
    FormatRepository formatRepository;

    @InjectMocks
    ValidationService validationService;

    @BeforeEach
    void setUp() {
        Format gen9ou = new Format();
        gen9ou.setId("gen9ou");
        gen9ou.setName("Gen 9 OU");
        gen9ou.setTeamSize(6);
        gen9ou.setBannedPokemon(List.of("flutter-mane", "iron-bundle"));
        when(formatRepository.findById("gen9ou")).thenReturn(Optional.of(gen9ou));
    }

    @Test
    void validTeamPasses() {
        List<String> team = List.of("garchomp", "landorus-therian", "volcarona", "iron-valiant", "great-tusk", "kingambit");
        ValidationResult result = validationService.validateTeam(team, "gen9ou");
        assertThat(result.valid()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void bannedPokemonFails() {
        List<String> team = List.of("flutter-mane", "garchomp", "landorus-therian", "volcarona", "iron-valiant", "great-tusk");
        ValidationResult result = validationService.validateTeam(team, "gen9ou");
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).contains("flutter-mane");
    }

    @Test
    void wrongSizeFails() {
        List<String> team = List.of("garchomp", "landorus-therian");
        ValidationResult result = validationService.validateTeam(team, "gen9ou");
        assertThat(result.valid()).isFalse();
        assertThat(result.errors()).isNotEmpty();
    }
}
