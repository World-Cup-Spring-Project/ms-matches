package br.com.infnet.msmatches.infra.kafka.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchCandidateEvent(
        String playerName,
        BigDecimal matchRating
) {}
