package com.pokemon.gamedata.config;

import com.pokemon.gamedata.client.PokeApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PokeApiConfig {

    @Bean
    public RestClient pokeApiRestClient() {
        return RestClient.builder()
                .baseUrl("https://pokeapi.co/api/v2")
                .build();
    }

    @Bean
    public PokeApiClient pokeApiClient(RestClient pokeApiRestClient) {
        return new PokeApiClient(pokeApiRestClient);
    }
}
