package com.pokemon.gamedata.controller;

import com.pokemon.gamedata.domain.Pokemon;
import com.pokemon.gamedata.service.PokemonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PokemonController.class)
class PokemonControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PokemonService pokemonService;

    @Test
    void getById_returns200() throws Exception {
        Pokemon p = new Pokemon();
        p.setId("pikachu");
        p.setName("Pikachu");
        when(pokemonService.findById("pikachu")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/pokemon/pikachu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pikachu"))
                .andExpect(jsonPath("$.name").value("Pikachu"));
    }

    @Test
    void getById_returns404() throws Exception {
        when(pokemonService.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pokemon/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAll_returns200() throws Exception {
        when(pokemonService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk());
    }
}
