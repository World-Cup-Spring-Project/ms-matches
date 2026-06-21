package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.client.CoreDataClient;
import br.com.infnet.msmatches.client.dto.CoreDataGameResponse;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.dto.response.MatchSyncResponse;
import br.com.infnet.msmatches.exception.InvalidStatusChangeException;
import br.com.infnet.msmatches.exception.MatchNotFoundException;
import br.com.infnet.msmatches.mapper.CoreDataGameMapper;
import br.com.infnet.msmatches.repository.MatchRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private static final Logger log = LoggerFactory.getLogger(MatchSyncService.class);

    private final CoreDataClient coreDataClient;
    private final CoreDataGameMapper coreDataGameMapper;
    private final MatchRepository matchRepository;
    private final MatchService matchService;

    public MatchSyncResponse syncAll() {
        List<CoreDataGameResponse> games = coreDataClient.fetchGames();
        int created = 0;
        int updated = 0;

        for (CoreDataGameResponse game : games) {
            if (!StringUtils.hasText(game.id())) {
                continue;
            }

            if (upsertGame(game)) {
                created++;
            } else {
                updated++;
            }
        }

        return new MatchSyncResponse(created, updated, games.size());
    }

    public Match syncGame(String externalMatchId) {
        CoreDataGameResponse game = coreDataClient.fetchGame(externalMatchId);
        if (game == null) {
            throw new MatchNotFoundException(externalMatchId);
        }

        upsertGame(game);
        return matchRepository.findByExternalMatchId(externalMatchId)
                .orElseThrow(() -> new MatchNotFoundException(externalMatchId));
    }

    private boolean upsertGame(CoreDataGameResponse game) {
        Optional<Match> existing = matchRepository.findByExternalMatchId(game.id());
        Match match;
        MatchStatus previousStatus;

        if (existing.isEmpty()) {
            match = coreDataGameMapper.toNewMatch(game);
            previousStatus = null;
        } else {
            match = existing.get();
            previousStatus = match.getStatus();
            coreDataGameMapper.applyUpdate(match, game);
        }

        MatchStatus mappedStatus = match.getStatus();
        boolean shouldPublishFinished = mappedStatus == MatchStatus.FINISHED
                && previousStatus != MatchStatus.FINISHED
                && previousStatus != MatchStatus.POST_MATCH_CLOSED;

        if (shouldPublishFinished) {
            revertFinishedMapping(match, previousStatus);
        }

        matchRepository.save(match);

        if (shouldPublishFinished) {
            publishFinishedFromSync(match, game.id());
        }

        return existing.isEmpty();
    }

    private void revertFinishedMapping(Match match, MatchStatus previousStatus) {
        if (previousStatus == null) {
            match.setStatus(MatchStatus.SCHEDULED);
            match.setFinished(false);
            return;
        }

        match.setStatus(previousStatus);
        match.setFinished(isFinishedStatus(previousStatus));
    }

    private void publishFinishedFromSync(Match match, String externalMatchId) {
        try {
            matchService.transitionToFinished(match, "sync-" + externalMatchId);
        } catch (InvalidStatusChangeException exception) {
            log.warn("Sync kept match {} at status {}: {}",
                    externalMatchId, match.getStatus(), exception.getMessage());
        }
    }

    private boolean isFinishedStatus(MatchStatus status) {
        return MatchStatus.FINISHED.equals(status) || MatchStatus.POST_MATCH_CLOSED.equals(status);
    }
}
