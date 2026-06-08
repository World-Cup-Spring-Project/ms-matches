package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.client.CoreDataClient;
import br.com.infnet.msmatches.domain.Match;
import br.com.infnet.msmatches.domain.TimelineEvent;
import br.com.infnet.msmatches.exception.InvalidMatchReferenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CoreDataReferenceValidator {

    private final CoreDataClient coreDataClient;

    public void validateMatchReferences(Match match) {
        if (!coreDataClient.isValidationEnabled()) {
            return;
        }

        validateStadium(match.getStadiumId());
        validateTeamIfDefined(match.getHomeTeamId());
        validateTeamIfDefined(match.getAwayTeamId());
    }

    public void validateTimelineEventReference(TimelineEvent event) {
        if (!coreDataClient.isValidationEnabled() || !StringUtils.hasText(event.getTeamId())) {
            return;
        }

        validateTeam(event.getTeamId());
    }

    private void validateStadium(String stadiumId) {
        if (!coreDataClient.stadiumExists(stadiumId)) {
            throw new InvalidMatchReferenceException("Stadium not found in ms-core-data: " + stadiumId);
        }
    }

    private void validateTeamIfDefined(String teamId) {
        if (!StringUtils.hasText(teamId) || "0".equals(teamId)) {
            return;
        }

        validateTeam(teamId);
    }

    private void validateTeam(String teamId) {
        if (!coreDataClient.teamExists(teamId)) {
            throw new InvalidMatchReferenceException("Team not found in ms-core-data: " + teamId);
        }
    }
}
