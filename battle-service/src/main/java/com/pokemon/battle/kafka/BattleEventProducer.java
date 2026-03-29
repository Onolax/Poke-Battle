package com.pokemon.battle.kafka;

import com.pokemon.battle.dto.BattleEndedEvent;
import com.pokemon.battle.dto.BattleStartedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BattleEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BattleEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBattleStarted(BattleStartedEvent event) {
        kafkaTemplate.send("battle.started", event.battleId(), event);
    }

    public void sendBattleEnded(BattleEndedEvent event) {
        kafkaTemplate.send("battle.ended", event.battleId(), event);
    }
}
