package br.com.infnet.msmatches.infra.kafka;

import br.com.infnet.msmatches.config.MatchesProperties;
import br.com.infnet.msmatches.infra.kafka.events.MatchStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchStatusChangedPublisher {

    private static final Logger log = LoggerFactory.getLogger(MatchStatusChangedPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MatchesProperties properties;

    public MatchStatusChangedPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                       MatchesProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public void publish(MatchStatusChangedEvent event) {
        log.info("Publishing MatchStatusChanged match={} status={} correlationId={}",
                event.matchId(), event.status(), event.correlationId());
        kafkaTemplate.send(
                properties.kafka().topics().matchStatusChanged(),
                event.matchId(),
                event
        );
    }
}
