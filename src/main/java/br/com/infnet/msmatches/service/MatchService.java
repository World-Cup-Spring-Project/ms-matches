package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.domain.model.TimelineEvent;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import br.com.infnet.msmatches.exception.MatchNotFoundException;
import br.com.infnet.msmatches.repository.MatchRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final CoreDataReferenceValidator coreDataReferenceValidator;

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

    public Match changeStatus(String id, MatchStatus status) {
        Match match = findById(id);
        match.changeStatus(status);
        return matchRepository.save(match);
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
}
