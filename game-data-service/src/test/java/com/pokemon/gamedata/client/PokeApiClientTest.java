package com.pokemon.gamedata.client;

import com.pokemon.gamedata.client.dto.PokeApiPokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokeApiClientTest {

    @Mock RestClient restClient;
    @Mock RestClient.RequestHeadersUriSpec<?> uriSpec;
    @Mock RestClient.RequestHeadersSpec<?> headersSpec;
    @Mock RestClient.ResponseSpec responseSpec;

    PokeApiClient client;

    @BeforeEach
    void setUp() {
        client = new PokeApiClient(restClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void fetchesPokemonBySlug() {
        PokeApiPokemon pokemon = new PokeApiPokemon(
                1, "bulbasaur",
                List.of(new PokeApiPokemon.StatEntry(45, new PokeApiPokemon.StatName("hp"))),
                List.of(new PokeApiPokemon.TypeEntry(new PokeApiPokemon.TypeName("grass"))),
                List.of(new PokeApiPokemon.AbilityEntry(new PokeApiPokemon.AbilityName("overgrow"), false)),
                69
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) uriSpec);
        when(uriSpec.uri(eq("/pokemon/{slug}"), eq("bulbasaur"))).thenReturn((RestClient.RequestHeadersSpec) headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(PokeApiPokemon.class)).thenReturn(pokemon);

        PokeApiPokemon result = client.fetchPokemon("bulbasaur");

        assertThat(result.name()).isEqualTo("bulbasaur");
        assertThat(result.weight()).isEqualTo(69);
    }
}
