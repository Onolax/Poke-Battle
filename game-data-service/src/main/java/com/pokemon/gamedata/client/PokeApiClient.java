package com.pokemon.gamedata.client;

import com.pokemon.gamedata.client.dto.PokeApiMove;
import com.pokemon.gamedata.client.dto.PokeApiPokemon;
import org.springframework.web.client.RestClient;

public class PokeApiClient {

    private final RestClient restClient;

    public PokeApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public PokeApiPokemon fetchPokemon(String slug) {
        return restClient.get()
                .uri("/pokemon/{slug}", slug)
                .retrieve()
                .body(PokeApiPokemon.class);
    }

    public PokeApiMove fetchMove(String slug) {
        return restClient.get()
                .uri("/move/{slug}", slug)
                .retrieve()
                .body(PokeApiMove.class);
    }
}
