package br.com.infnet.msmatches.dto.squad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SquadPlayerJson(
        int number,
        String name,
        String pos
) {
}
