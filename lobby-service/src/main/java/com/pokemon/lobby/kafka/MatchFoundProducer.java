package com.pokemon.lobby.kafka;

import com.pokemon.lobby.dto.MatchFoundEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MatchFoundProducer {

    static final String TOPIC = "match.found";

    private final KafkaTemplate<String, MatchFoundEvent> kafkaTemplate;

    public MatchFoundProducer(KafkaTemplate<String, MatchFoundEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(MatchFoundEvent event) {
        kafkaTemplate.send(TOPIC, event.battleId(), event);
    }
}
