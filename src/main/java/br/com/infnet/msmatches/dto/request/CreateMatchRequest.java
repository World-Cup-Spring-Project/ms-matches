package br.com.infnet.msmatches.dto.request;

import br.com.infnet.msmatches.domain.enums.MatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateMatchRequest(
        String externalMatchId,
        @NotNull Integer matchday,
        String group,
        String type,
        @NotBlank String stadiumId,
        String homeTeamId,
        String awayTeamId,
        Integer homeScore,
        Integer awayScore,
        String homeTeamLabel,
        String awayTeamLabel,
        MatchStatus status,
        LocalDate localDate,
        String rawLocalDate
) {
}
