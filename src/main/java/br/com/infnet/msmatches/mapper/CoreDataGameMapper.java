package br.com.infnet.msmatches.mapper;

import br.com.infnet.msmatches.client.dto.CoreDataGameResponse;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import br.com.infnet.msmatches.domain.model.Match;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CoreDataGameMapper {

    private static final DateTimeFormatter LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public Match toNewMatch(CoreDataGameResponse game) {
        Instant now = Instant.now();
        Match match = applyFields(Match.builder().build(), game);
        match.setCreatedAt(now);
        match.setUpdatedAt(now);
        return match;
    }

    public void applyUpdate(Match match, CoreDataGameResponse game) {
        applyFields(match, game);
        match.setUpdatedAt(Instant.now());
    }

    private Match applyFields(Match match, CoreDataGameResponse game) {
        match.setExternalMatchId(game.id());
        match.setHomeTeamId(game.homeTeamId());
        match.setAwayTeamId(game.awayTeamId());
        match.setHomeScore(parseInteger(game.homeScore()));
        match.setAwayScore(parseInteger(game.awayScore()));
        match.setGroup(game.group());
        match.setMatchday(parseInteger(game.matchday()));
        match.setStadiumId(game.stadiumId());
        match.setType(game.type());
        match.setRawLocalDate(game.localDate());
        match.setLocalDate(parseLocalDate(game.localDate()));
        match.setHomeTeamLabel(resolveTeamLabel(game.homeTeamLabel(), game.homeTeamNameEn()));
        match.setAwayTeamLabel(resolveTeamLabel(game.awayTeamLabel(), game.awayTeamNameEn()));
        match.changeStatus(deriveStatus(game, match.getStatus()));
        return match;
    }

    private MatchStatus deriveStatus(CoreDataGameResponse game, MatchStatus currentStatus) {
        if (currentStatus != null && currentStatus.marksAsFinished()) {
            return currentStatus;
        }

        if (parseBoolean(game.finished())) {
            return MatchStatus.FINISHED;
        }

        String timeElapsed = game.timeElapsed();
        if (!StringUtils.hasText(timeElapsed) || "notstarted".equalsIgnoreCase(timeElapsed)) {
            return MatchStatus.SCHEDULED;
        }

        if ("halftime".equalsIgnoreCase(timeElapsed) || "half_time".equalsIgnoreCase(timeElapsed)) {
            return MatchStatus.HALF_TIME;
        }

        return MatchStatus.LIVE;
    }

    private String resolveTeamLabel(String label, String teamName) {
        if (StringUtils.hasText(label)) {
            return label;
        }
        return teamName;
    }

    private Integer parseInteger(String value) {
        if (!StringUtils.hasText(value) || "null".equalsIgnoreCase(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "TRUE".equals(value);
    }

    private LocalDate parseLocalDate(String rawLocalDate) {
        if (!StringUtils.hasText(rawLocalDate)) {
            return null;
        }

        String datePart = rawLocalDate.trim().split("\\s+")[0];
        try {
            return LocalDate.parse(datePart, LOCAL_DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
