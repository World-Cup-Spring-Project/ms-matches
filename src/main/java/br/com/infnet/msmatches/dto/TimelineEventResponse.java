package br.com.infnet.msmatches.dto;

import br.com.infnet.msmatches.domain.enums.EventType;
import java.time.Instant;

public record TimelineEventResponse(
        EventType type,
        Integer minute,
        Integer stoppageMinute,
        String player,
        String teamId,
        String description,
        Instant occurredAt
) {
}
