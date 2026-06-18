package br.com.infnet.msmatches.client;

import br.com.infnet.msmatches.client.dto.CoreDataGameResponse;
import br.com.infnet.msmatches.client.dto.CoreDataGameWrapperResponse;
import br.com.infnet.msmatches.client.dto.CoreDataGamesListResponse;
import br.com.infnet.msmatches.config.CoreDataProperties;
import br.com.infnet.msmatches.exception.CoreDataUnavailableException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class CoreDataClient {

    private final RestClient coreDataRestClient;
    private final CoreDataProperties properties;
    private final ObjectMapper objectMapper;

    public boolean isValidationEnabled() {
        return properties.validationEnabled();
    }

    public boolean teamExists(String teamId) {
        return exists("/teams/{teamId}", teamId);
    }

    public boolean stadiumExists(String stadiumId) {
        return exists("/stadiums/{stadiumId}", stadiumId);
    }

    public List<CoreDataGameResponse> fetchGames() {
        try {
            byte[] body = coreDataRestClient.get()
                    .uri(properties.gamesPath())
                    .retrieve()
                    .body(byte[].class);

            if (body == null || body.length == 0) {
                return List.of();
            }

            return parseGamesList(body);
        } catch (ResourceAccessException exception) {
            throw new CoreDataUnavailableException("ms-core-data is unavailable", exception);
        } catch (RestClientException exception) {
            throw new CoreDataUnavailableException("Could not fetch games from ms-core-data", exception);
        }
    }

    public CoreDataGameResponse fetchGame(String gameId) {
        try {
            byte[] body = coreDataRestClient.get()
                    .uri(properties.gameByIdPath(), gameId)
                    .retrieve()
                    .body(byte[].class);

            if (body == null || body.length == 0) {
                throw new CoreDataUnavailableException(
                        "Empty response for game " + gameId,
                        new IllegalStateException("empty body")
                );
            }

            return parseGame(body);
        } catch (HttpClientErrorException.NotFound exception) {
            return null;
        } catch (ResourceAccessException exception) {
            throw new CoreDataUnavailableException("ms-core-data is unavailable", exception);
        } catch (RestClientException exception) {
            throw new CoreDataUnavailableException("Could not fetch game from ms-core-data", exception);
        }
    }

    private boolean exists(String uri, String value) {
        if (!properties.validationEnabled()) {
            return true;
        }

        try {
            ResponseEntity<Void> response = coreDataRestClient.get()
                    .uri(uri, value)
                    .retrieve()
                    .toBodilessEntity();
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (ResourceAccessException exception) {
            throw new CoreDataUnavailableException("ms-core-data is unavailable", exception);
        } catch (RestClientException exception) {
            throw new CoreDataUnavailableException("Could not validate reference in ms-core-data", exception);
        }
    }

    private List<CoreDataGameResponse> parseGamesList(byte[] body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            if (root.isArray()) {
                return objectMapper.convertValue(root, new TypeReference<>() {
                });
            }

            if (root.has("games")) {
                CoreDataGamesListResponse wrapper = objectMapper.treeToValue(root, CoreDataGamesListResponse.class);
                return wrapper.games() != null ? wrapper.games() : List.of();
            }

            throw new CoreDataUnavailableException(
                    "Unexpected games response format from ms-core-data",
                    new IllegalStateException("missing games array")
            );
        } catch (CoreDataUnavailableException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new CoreDataUnavailableException("Could not parse games response from ms-core-data", exception);
        }
    }

    private CoreDataGameResponse parseGame(byte[] body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            if (root.has("game")) {
                CoreDataGameWrapperResponse wrapper = objectMapper.treeToValue(root, CoreDataGameWrapperResponse.class);
                return wrapper.game();
            }

            return objectMapper.treeToValue(root, CoreDataGameResponse.class);
        } catch (Exception exception) {
            throw new CoreDataUnavailableException("Could not parse game response from ms-core-data", exception);
        }
    }
}
