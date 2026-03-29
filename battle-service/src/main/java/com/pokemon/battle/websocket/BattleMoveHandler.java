package com.pokemon.battle.websocket;

import com.pokemon.battle.dto.MoveAction;
import com.pokemon.battle.service.TurnResolutionService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class BattleMoveHandler {

    private final TurnResolutionService turnService;

    public BattleMoveHandler(TurnResolutionService turnService) {
        this.turnService = turnService;
    }

    @MessageMapping("/battle/{battleId}/move")
    public void handleMove(@DestinationVariable String battleId,
                           @Header("userId") String userId,
                           Map<String, String> payload) {
        turnService.submitAction(battleId, userId, MoveAction.move(payload.get("moveSlug")));
    }

    @MessageMapping("/battle/{battleId}/switch")
    public void handleSwitch(@DestinationVariable String battleId,
                              @Header("userId") String userId,
                              Map<String, String> payload) {
        turnService.submitAction(battleId, userId, MoveAction.switchPokemon(payload.get("slot")));
    }

    @MessageMapping("/battle/{battleId}/forfeit")
    public void handleForfeit(@DestinationVariable String battleId,
                               @Header("userId") String userId) {
        turnService.submitAction(battleId, userId, MoveAction.forfeit());
    }
}
