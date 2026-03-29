package com.pokemon.rating.kafka;

import com.pokemon.rating.dto.BattleEndedEvent;
import com.pokemon.rating.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BattleEndedConsumer {

    private static final Logger log = LoggerFactory.getLogger(BattleEndedConsumer.class);

    private final RatingService ratingService;

    public BattleEndedConsumer(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @KafkaListener(topics = "battle.ended", groupId = "rating-service")
    public void onBattleEnded(BattleEndedEvent event) {
        log.info("Processing battle result: {} beat {} in battle {}", event.winnerId(), event.loserId(), event.battleId());
        ratingService.processBattleResult(event);
    }
}
