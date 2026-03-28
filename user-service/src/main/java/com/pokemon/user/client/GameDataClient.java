package com.pokemon.user.client;

import com.pokemon.user.client.dto.TeamValidationRequest;
import com.pokemon.user.client.dto.TeamValidationResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class GameDataClient {

    private final RestClient restClient;

    public GameDataClient(RestClient gameDataRestClient) {
        this.restClient = gameDataRestClient;
    }

    public TeamValidationResult validateTeam(List<String> slugs, String formatId) {
        try {
            TeamValidationResult result = restClient.post()
                    .uri("/api/validate/team")
                    .body(new TeamValidationRequest(slugs, formatId))
                    .retrieve()
                    .body(TeamValidationResult.class);
            if (result == null) {
                throw new GameDataUnavailableException("game-data-service returned empty body", null);
            }
            return result;
        } catch (GameDataUnavailableException e) {
            throw e;
        } catch (RestClientException e) {
            throw new GameDataUnavailableException("game-data-service unavailable", e);
        }
    }
}
