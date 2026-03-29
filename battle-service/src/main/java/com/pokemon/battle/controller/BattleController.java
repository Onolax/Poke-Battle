package com.pokemon.battle.controller;

import com.pokemon.battle.dto.BattleState;
import com.pokemon.battle.dto.MoveAction;
import com.pokemon.battle.redis.BattleStateRepository;
import com.pokemon.battle.service.TurnResolutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/battle")
public class BattleController {

    private final BattleStateRepository repo;
    private final TurnResolutionService turnService;

    public BattleController(BattleStateRepository repo, TurnResolutionService turnService) {
        this.repo = repo;
        this.turnService = turnService;
    }

    @GetMapping("/{battleId}/state")
    public ResponseEntity<BattleState> getState(@PathVariable String battleId) {
        BattleState state = repo.loadState(battleId);
        return state != null ? ResponseEntity.ok(state) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{battleId}/forfeit")
    public ResponseEntity<Void> forfeit(@PathVariable String battleId,
                                         @RequestHeader("X-User-Id") String userId) {
        turnService.submitAction(battleId, userId, MoveAction.forfeit());
        return ResponseEntity.ok().build();
    }
}
