package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.domain.model.TimelineEvent;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import br.com.infnet.msmatches.dto.request.ChangeMatchStatusRequest;
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

    private final MatchRepository matchRepository;
    private final CoreDataReferenceValidator coreDataReferenceValidator;
    private final MatchStatusChangedPublisher matchStatusChangedPublisher;
    private final MatchRatingService matchRatingService;

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
        return resolveMatch(id);
    }

    public Match changeStatus(String id, ChangeMatchStatusRequest request) {
        Match match = resolveMatch(id);
        match.changeStatus(request.status());
        Match saved = matchRepository.save(match);

        String correlationId = request.correlationId() != null
                ? request.correlationId()
                : UUID.randomUUID().toString();

        List<MatchCandidateEvent> candidates = request.status() == MatchStatus.FINISHED
                ? matchRatingService.generateTopCandidates(saved)
                : null;

        matchStatusChangedPublisher.publish(new MatchStatusChangedEvent(
                saved.getId(),
                saved.getStatus().name(),
                correlationId,
                Instant.now(),
                candidates
        ));

        return saved;
    }

    public Match addTimelineEvent(String id, TimelineEvent event) {
        coreDataReferenceValidator.validateTimelineEventReference(event);

        Match match = resolveMatch(id);
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(Instant.now());
        }
        match.addTimelineEvent(event);
        return matchRepository.save(match);
    }

    private Match resolveMatch(String id) {
        return matchRepository.findById(id)
                .or(() -> matchRepository.findByExternalMatchId(id))
                .orElseThrow(() -> new MatchNotFoundException(id));
    }
}
