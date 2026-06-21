package br.com.infnet.msmatches.dto.response;

import br.com.infnet.msmatches.domain.enums.MatchStatus;
import java.time.Instant;
import java.time.LocalDate;

public record MatchResponse(
        String id,
        String externalMatchId,
        Integer matchday,
        String group,
        String type,
        String stadiumId,
        String homeTeamId,
        String awayTeamId,
        Integer homeScore,
        Integer awayScore,
        String homeTeamLabel,
        String awayTeamLabel,
        MatchStatus status,
        Boolean finished,
        LocalDate localDate,
        String rawLocalDate,
        Instant createdAt,
        Instant updatedAt
) {
}
