package br.com.infnet.msmatches.mapper;

import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.dto.request.CreateMatchRequest;
import br.com.infnet.msmatches.dto.response.MatchResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    public Match toDomain(CreateMatchRequest request) {
        return Match.builder()
                .externalMatchId(request.externalMatchId())
                .matchday(request.matchday())
                .group(request.group())
                .type(request.type())
                .stadiumId(request.stadiumId())
                .homeTeamId(request.homeTeamId())
                .awayTeamId(request.awayTeamId())
                .homeScore(request.homeScore() == null ? 0 : request.homeScore())
                .awayScore(request.awayScore() == null ? 0 : request.awayScore())
                .homeTeamLabel(request.homeTeamLabel())
                .awayTeamLabel(request.awayTeamLabel())
                .status(request.status())
                .localDate(request.localDate())
                .rawLocalDate(request.rawLocalDate())
                .build();
    }

    public MatchResponse toResponse(Match match) {
        return new MatchResponse(
                match.getId(),
                match.getExternalMatchId(),
                match.getMatchday(),
                match.getGroup(),
                match.getType(),
                match.getStadiumId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getHomeTeamLabel(),
                match.getAwayTeamLabel(),
                match.getStatus(),
                match.getFinished(),
                match.getLocalDate(),
                match.getRawLocalDate(),
                match.getCreatedAt(),
                match.getUpdatedAt()
        );
    }

    public List<MatchResponse> toResponses(List<Match> matches) {
        return matches.stream()
                .map(this::toResponse)
                .toList();
    }
}
