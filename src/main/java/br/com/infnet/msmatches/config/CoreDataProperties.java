package br.com.infnet.msmatches.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integrations.core-data")
public record CoreDataProperties(
        String baseUrl,
        boolean validationEnabled
) {
}
