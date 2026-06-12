package br.com.infnet.msmatches.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arenacup")
public record MatchesProperties(Kafka kafka) {

    public record Kafka(Topics topics) {}

    public record Topics(String matchStatusChanged) {}
}
