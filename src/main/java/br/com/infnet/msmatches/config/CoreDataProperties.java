package br.com.infnet.msmatches.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integrations.core-data")
public record CoreDataProperties(
        String baseUrl,
        boolean validationEnabled,
        boolean syncEnabled,
        boolean syncOnStartup,
        long syncIntervalMs,
        String gamesPath,
        String gameByIdPath
) {

    public CoreDataProperties {
        if (gamesPath == null || gamesPath.isBlank()) {
            gamesPath = "/games";
        }
        if (gameByIdPath == null || gameByIdPath.isBlank()) {
            gameByIdPath = "/games/{gameId}";
        }
        if (syncIntervalMs <= 0) {
            syncIntervalMs = 300_000L;
        }
    }
}
