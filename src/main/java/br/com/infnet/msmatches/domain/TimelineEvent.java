package br.com.infnet.msmatches.domain;

import br.com.infnet.msmatches.domain.enums.EventType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {

    private EventType type;
    private Integer minute;
    private Integer stoppageMinute;
    private String player;
    private String teamId;
    private String description;
    private Instant occurredAt;
}
