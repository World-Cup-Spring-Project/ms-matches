package br.com.infnet.msmatches.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoreDataGameWrapperResponse(
        @JsonProperty("game") CoreDataGameResponse game
) {
}
