package br.com.infnet.msmatches.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoreDataGamesListResponse(
        @JsonProperty("games") List<CoreDataGameResponse> games
) {
}
