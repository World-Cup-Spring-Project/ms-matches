package br.com.infnet.msmatches.infra.kafka.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchStatusChangedEvent(
        String matchId,
        String status,
        String correlationId,
        Instant occurredAt,
        List<MatchCandidateEvent> candidates
) {}
