package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import br.com.infnet.msmatches.dto.request.ChangeMatchStatusRequest;
import br.com.infnet.msmatches.exception.InvalidStatusChangeException;
import br.com.infnet.msmatches.exception.MatchNotFoundException;
import br.com.infnet.msmatches.infra.kafka.MatchStatusChangedPublisher;
import br.com.infnet.msmatches.infra.kafka.events.MatchCandidateEvent;
import br.com.infnet.msmatches.infra.kafka.events.MatchStatusChangedEvent;
import br.com.infnet.msmatches.repository.MatchRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchStatusChangedPublisher matchStatusChangedPublisher;
    private final MatchRatingService matchRatingService;

    public Match create(Match match) {
        Instant now = Instant.now();
        match.setCreatedAt(now);
        match.setUpdatedAt(now);
        MatchStatus initialStatus = match.getStatus() != null ? match.getStatus() : MatchStatus.SCHEDULED;
        match.changeStatus(initialStatus);
        return matchRepository.save(match);
    }

    public List<Match> findAll(MatchStatus status) {
        if (status == null) {
            return matchRepository.findAll();
        }
        return matchRepository.findByStatus(status);
    }

    public Match findById(String id) {
        return resolveMatch(id);
    }

    public Match changeStatus(String id, ChangeMatchStatusRequest request) {
        Match match = resolveMatch(id);
        String correlationId = resolveCorrelationId(request.correlationId());

        if (request.status() == MatchStatus.FINISHED) {
            return transitionToFinished(match, correlationId);
        }

        match.changeStatus(request.status());
        Match saved = matchRepository.save(match);

        if (request.status().marksAsFinished()) {
            publishStatusChange(saved, null, correlationId);
        }

        return saved;
    }

    public void saveSyncedMatch(Match match, MatchStatus previousStatus) {
        MatchStatus mappedStatus = match.getStatus();
        boolean shouldPublishFinished = shouldPublishFinishedOnSync(mappedStatus, previousStatus);

        if (shouldPublishFinished) {
            match.changeStatus(statusBeforeSyncFinishedTransition(previousStatus));
        }

        matchRepository.save(match);

        if (!shouldPublishFinished) {
            return;
        }

        try {
            transitionToFinished(match, "sync-" + match.getExternalMatchId());
        } catch (InvalidStatusChangeException exception) {
            log.warn("Sync kept match {} at status {}: {}",
                    match.getExternalMatchId(), match.getStatus(), exception.getMessage());
        }
    }

    public Match transitionToFinished(Match match, String correlationId) {
        List<MatchCandidateEvent> candidates = matchRatingService.generateTopCandidates(match);
        match.changeStatus(MatchStatus.FINISHED);
        Match saved = matchRepository.save(match);
        publishStatusChange(saved, candidates, correlationId);
        return saved;
    }

    private Match resolveMatch(String id) {
        return matchRepository.findById(id)
                .or(() -> matchRepository.findByExternalMatchId(id))
                .orElseThrow(() -> new MatchNotFoundException(id));
    }

    private void publishStatusChange(Match match, List<MatchCandidateEvent> candidates, String correlationId) {
        matchStatusChangedPublisher.publish(new MatchStatusChangedEvent(
                resolveEventMatchId(match),
                match.getStatus().name(),
                correlationId,
                Instant.now(),
                candidates
        ));
    }

    private String resolveEventMatchId(Match match) {
        if (StringUtils.hasText(match.getExternalMatchId())) {
            return match.getExternalMatchId();
        }
        return match.getId();
    }

    private String resolveCorrelationId(String correlationId) {
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    private boolean shouldPublishFinishedOnSync(MatchStatus mappedStatus, MatchStatus previousStatus) {
        return mappedStatus == MatchStatus.FINISHED
                && previousStatus != MatchStatus.FINISHED
                && previousStatus != MatchStatus.POST_MATCH_CLOSED;
    }

    private MatchStatus statusBeforeSyncFinishedTransition(MatchStatus previousStatus) {
        return previousStatus == null ? MatchStatus.SCHEDULED : previousStatus;
    }
}
