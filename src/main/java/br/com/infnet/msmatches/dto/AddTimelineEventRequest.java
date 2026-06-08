package br.com.infnet.msmatches.dto;

import br.com.infnet.msmatches.domain.enums.EventType;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record AddTimelineEventRequest(
        @NotNull EventType type,
        @NotNull Integer minute,
        Integer stoppageMinute,
        String player,
        String teamId,
        String description,
        Instant occurredAt
) {
}
