package br.com.infnet.msmatches.domain.model;

import br.com.infnet.msmatches.domain.enums.MatchStatus;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "matches")
public class Match {

    @Id
    private String id;

    private String externalMatchId;
    private Integer matchday;
    private String group;
    private String type;
    private String stadiumId;
    private String homeTeamId;
    private String awayTeamId;
    private Integer homeScore;
    private Integer awayScore;
    private String homeTeamLabel;
    private String awayTeamLabel;

    @Builder.Default
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Builder.Default
    private Boolean finished = false;

    private LocalDate localDate;
    private String rawLocalDate;
    private Instant createdAt;
    private Instant updatedAt;

    public void changeStatus(MatchStatus nextStatus) {
        status = nextStatus;
        finished = MatchStatus.FINISHED.equals(nextStatus)
                || MatchStatus.POST_MATCH_CLOSED.equals(nextStatus);
        updatedAt = Instant.now();
    }
}
