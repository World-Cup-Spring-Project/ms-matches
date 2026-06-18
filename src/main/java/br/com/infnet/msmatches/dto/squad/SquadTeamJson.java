package br.com.infnet.msmatches.dto.squad;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SquadTeamJson(
        String name,
        String fifa_code,
        List<SquadPlayerJson> players
) {
}
