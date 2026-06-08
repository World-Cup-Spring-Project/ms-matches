package br.com.infnet.msmatches.client;

import br.com.infnet.msmatches.config.CoreDataProperties;
import br.com.infnet.msmatches.exception.CoreDataUnavailableException;
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

    public boolean isValidationEnabled() {
        return properties.validationEnabled();
    }

    public boolean teamExists(String teamId) {
        return exists("/teams/{teamId}", teamId);
    }

    public boolean stadiumExists(String stadiumId) {
        return exists("/stadiums/{stadiumId}", stadiumId);
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
}
