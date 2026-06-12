package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.domain.model.TimelineEvent;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import br.com.infnet.msmatches.dto.request.ChangeMatchStatusRequest;
import br.com.infnet.msmatches.dto.request.MatchCandidateRequest;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchService {

    private static final int REQUIRED_CANDIDATES = 3;

    private final MatchRepository matchRepository;
    private final CoreDataReferenceValidator coreDataReferenceValidator;
    private final MatchStatusChangedPublisher matchStatusChangedPublisher;

    public Match create(Match match) {
        coreDataReferenceValidator.validateMatchReferences(match);

        Instant now = Instant.now();
        match.setCreatedAt(now);
        match.setUpdatedAt(now);
        if (match.getStatus() == null) {
            match.setStatus(MatchStatus.SCHEDULED);
        }
        match.setFinished(MatchStatus.FINISHED.equals(match.getStatus()));
        return matchRepository.save(match);
    }

    public List<Match> findAll(MatchStatus status) {
        if (status == null) {
            return matchRepository.findAll();
        }
        return matchRepository.findByStatus(status);
    }

    public Match findById(String id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new MatchNotFoundException(id));
    }

    public Match changeStatus(String id, ChangeMatchStatusRequest request) {
        validateStatusChange(request);

        Match match = findById(id);
        match.changeStatus(request.status());
        Match saved = matchRepository.save(match);

        String correlationId = request.correlationId() != null
                ? request.correlationId()
                : UUID.randomUUID().toString();

        matchStatusChangedPublisher.publish(new MatchStatusChangedEvent(
                saved.getId(),
                saved.getStatus().name(),
                correlationId,
                Instant.now(),
                toEventCandidates(request.candidates())
        ));

        return saved;
    }

    public Match addTimelineEvent(String id, TimelineEvent event) {
        coreDataReferenceValidator.validateTimelineEventReference(event);

        Match match = findById(id);
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }
        match.addTimelineEvent(event);
        return matchRepository.save(match);
    }

    private void validateStatusChange(ChangeMatchStatusRequest request) {
        if (request.status() != MatchStatus.FINISHED) {
            return;
        }

        List<MatchCandidateRequest> candidates = request.candidates();
        if (candidates == null || candidates.size() != REQUIRED_CANDIDATES) {
            throw new InvalidStatusChangeException(
                    "Status FINISHED requires exactly " + REQUIRED_CANDIDATES + " candidates");
        }
    }

    private List<MatchCandidateEvent> toEventCandidates(List<MatchCandidateRequest> candidates) {
        if (candidates == null) {
            return null;
        }
        return candidates.stream()
                .map(candidate -> new MatchCandidateEvent(candidate.playerId(), candidate.matchRating()))
                .toList();
    }
}
