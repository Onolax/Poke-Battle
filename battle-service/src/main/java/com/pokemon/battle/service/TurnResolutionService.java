package com.pokemon.battle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemon.battle.dto.*;
import com.pokemon.battle.engine.DamageCalculator;
import com.pokemon.battle.engine.StatusEffectProcessor;
import com.pokemon.battle.engine.WinConditionChecker;
import com.pokemon.battle.kafka.BattleEventProducer;
import com.pokemon.battle.redis.BattleStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TurnResolutionService {

    private static final Logger log = LoggerFactory.getLogger(TurnResolutionService.class);
    private static final String POD_ID = UUID.randomUUID().toString();

    private final BattleStateRepository repo;
    private final DamageCalculator damageCalc;
    private final StatusEffectProcessor statusProc;
    private final WinConditionChecker winChecker;
    private final BattleEventProducer eventProducer;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public TurnResolutionService(BattleStateRepository repo, DamageCalculator damageCalc,
                                  StatusEffectProcessor statusProc, WinConditionChecker winChecker,
                                  BattleEventProducer eventProducer,
                                  SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.repo = repo;
        this.damageCalc = damageCalc;
        this.statusProc = statusProc;
        this.winChecker = winChecker;
        this.eventProducer = eventProducer;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void submitAction(String battleId, String playerId, MoveAction action) {
        BattleState state = repo.loadState(battleId);
        if (state == null || !"ACTIVE".equals(state.phase)) return;

        String playerKey = playerId.equals(state.player1Id) ? "player1" : "player2";
        boolean added = repo.submitMove(battleId, playerKey, toJson(action));
        if (!added) return; // already submitted this turn

        var pending = repo.getPendingMoves(battleId);
        if (pending.size() < 2) return; // wait for both

        // Try to acquire lock — only one pod resolves the turn
        if (!repo.acquireLock(battleId, POD_ID)) return;

        try {
            resolveTurn(battleId, state,
                    fromJson(pending.get("player1").toString(), MoveAction.class),
                    fromJson(pending.get("player2").toString(), MoveAction.class));
        } finally {
            repo.clearPendingMoves(battleId);
            repo.releaseLock(battleId);
        }
    }

    private void resolveTurn(String battleId, BattleState state, MoveAction action1, MoveAction action2) {
        BattlePokemon[] team1 = repo.loadTeam(battleId, "p1");
        BattlePokemon[] team2 = repo.loadTeam(battleId, "p2");
        BattlePokemon active1 = team1[state.activeSlot1];
        BattlePokemon active2 = team2[state.activeSlot2];

        List<TurnResult.ActionResult> results = new ArrayList<>();

        // Determine move order: priority first, then speed (with PAR speed penalty)
        int speed1 = getPARAdjustedSpeed(active1);
        int speed2 = getPARAdjustedSpeed(active2);

        boolean p1First = determineFirst(action1, action2, speed1, speed2);

        // Resolve in order
        if (p1First) {
            resolveAction(battleId, state, action1, state.player1Id, active1, active2, team1, team2, state.activeSlot1, state.activeSlot2, results);
            if (!winChecker.isBattleOver(team1, team2)) {
                resolveAction(battleId, state, action2, state.player2Id, active2, active1, team2, team1, state.activeSlot2, state.activeSlot1, results);
            }
        } else {
            resolveAction(battleId, state, action2, state.player2Id, active2, active1, team2, team1, state.activeSlot2, state.activeSlot1, results);
            if (!winChecker.isBattleOver(team1, team2)) {
                resolveAction(battleId, state, action1, state.player1Id, active1, active2, team1, team2, state.activeSlot1, state.activeSlot2, results);
            }
        }

        // End-of-turn status damage
        applyEndOfTurnDamage(active1, results);
        applyEndOfTurnDamage(active2, results);

        state.turnNumber++;
        repo.saveState(state);
        repo.saveTeam(battleId, "p1", team1);
        repo.saveTeam(battleId, "p2", team2);

        TurnResult turnResult = buildTurnResult(state, team1, team2, results);

        if (winChecker.isBattleOver(team1, team2)) {
            endBattle(battleId, state, team1, team2, turnResult, "NORMAL");
        } else {
            broadcast(battleId, turnResult);
        }
    }

    private void resolveAction(String battleId, BattleState state, MoveAction action,
                                String actorId, BattlePokemon attacker, BattlePokemon defender,
                                BattlePokemon[] attackerTeam, BattlePokemon[] defenderTeam,
                                int attackerSlot, int defenderSlot,
                                List<TurnResult.ActionResult> results) {
        if (attacker.fainted) return;

        TurnResult.ActionResult result = new TurnResult.ActionResult();
        result.playerId = actorId;

        if ("FORFEIT".equals(action.type())) {
            result.actionType = "FORFEIT";
            result.message = attacker.name + " forfeited!";
            // Mark all attacker team as fainted
            for (BattlePokemon p : attackerTeam) p.fainted = true;
            results.add(result);
            return;
        }

        if ("SWITCH".equals(action.type())) {
            int slot = Integer.parseInt(action.value());
            if (slot >= 0 && slot < attackerTeam.length && !attackerTeam[slot].fainted && slot != attackerSlot) {
                if (state.player1Id.equals(actorId)) state.activeSlot1 = slot;
                else state.activeSlot2 = slot;
                result.actionType = "SWITCH";
                result.message = attacker.name + " switched out! " + attackerTeam[slot].name + " came in!";
            }
            results.add(result);
            return;
        }

        // MOVE
        if (!statusProc.canAct(attacker)) {
            result.actionType = "MOVE";
            result.message = attacker.name + " can't move!";
            results.add(result);
            return;
        }

        BattleMove move = attacker.moves.stream()
                .filter(m -> m.slug.equals(action.value()))
                .findFirst()
                .orElse(attacker.moves.get(0));

        move.currentPp = Math.max(0, move.currentPp - 1);
        statusProc.thawOnFireHit(defender, move.type);

        result.actionType = "MOVE";
        result.moveName = move.name;
        result.targetName = defender.name;

        if (move.basePower > 0) {
            double typeEff = damageCalc.getTypeEffectiveness(move.type, defender.types);
            if (typeEff == 0) {
                result.message = "It doesn't affect " + defender.name + "!";
                result.effectiveness = "IMMUNE";
            } else {
                int damage = damageCalc.calculate(attacker, defender, move);
                damage = Math.min(damage, defender.currentHp);
                defender.currentHp -= damage;
                if (defender.currentHp <= 0) { defender.currentHp = 0; defender.fainted = true; }

                result.damageDealt = damage;
                result.effectiveness = damageCalc.effectivenessLabel(typeEff);
                result.message = attacker.name + " used " + move.name + "!";
                if (!"NORMAL".equals(result.effectiveness))
                    result.message += " It's " + result.effectiveness.toLowerCase() + "!";

                // Secondary effect
                if (move.secondaryEffect != null && new Random().nextInt(100) < move.secondaryEffect.chance) {
                    if (move.secondaryEffect.effect != null && move.secondaryEffect.effect.length() <= 3) {
                        boolean applied = statusProc.applyStatus(defender, move.secondaryEffect.effect);
                        if (applied) result.statusApplied = move.secondaryEffect.effect;
                    }
                }
            }
        } else {
            result.message = attacker.name + " used " + move.name + "!";
            // Status move: apply status if specified in secondary effect
            if (move.secondaryEffect != null && move.secondaryEffect.effect != null
                    && move.secondaryEffect.effect.length() <= 3) {
                statusProc.applyStatus(defender, move.secondaryEffect.effect);
                result.statusApplied = move.secondaryEffect.effect;
            }
        }

        results.add(result);
    }

    private void endBattle(String battleId, BattleState state, BattlePokemon[] team1,
                            BattlePokemon[] team2, TurnResult lastTurnResult, String reason) {
        String winnerId = winChecker.isTeamDefeated(team1) ? state.player2Id : state.player1Id;
        String loserId = winnerId.equals(state.player1Id) ? state.player2Id : state.player1Id;
        String winnerUsername = winnerId.equals(state.player1Id) ? state.player1Username : state.player2Username;
        String loserUsername = loserId.equals(state.player1Id) ? state.player1Username : state.player2Username;

        state.phase = "ENDED";
        state.winnerId = winnerId;
        state.endReason = reason;
        repo.saveState(state);
        repo.setEndedTtl(battleId);

        // Build final message with battle end info
        Map<String, Object> endMsg = new LinkedHashMap<>();
        endMsg.put("type", "BATTLE_END");
        endMsg.put("winnerId", winnerId);
        endMsg.put("turnResult", lastTurnResult);

        String payload = toJson(endMsg);
        repo.publish(battleId, payload);
        messagingTemplate.convertAndSend("/topic/battle/" + battleId, payload);

        eventProducer.sendBattleEnded(new BattleEndedEvent(
                battleId, winnerId, winnerUsername, loserId, loserUsername,
                "GEN9OU", state.turnNumber));
    }

    private void broadcast(String battleId, TurnResult result) {
        String payload = toJson(result);
        repo.publish(battleId, payload);
        messagingTemplate.convertAndSend("/topic/battle/" + battleId, payload);
    }

    private boolean determineFirst(MoveAction a1, MoveAction a2, int speed1, int speed2) {
        int priority1 = getActionPriority(a1);
        int priority2 = getActionPriority(a2);
        if (priority1 != priority2) return priority1 > priority2;
        if (speed1 != speed2) return speed1 > speed2;
        return new Random().nextBoolean();
    }

    private int getActionPriority(MoveAction action) {
        if ("SWITCH".equals(action.type()) || "FORFEIT".equals(action.type())) return 6; // switches go first
        return 0; // simplified — full priority list would check move.priority
    }

    private int getPARAdjustedSpeed(BattlePokemon p) {
        return "PAR".equals(p.status) ? p.speed / 2 : p.speed;
    }

    private void applyEndOfTurnDamage(BattlePokemon pokemon, List<TurnResult.ActionResult> results) {
        int dmg = statusProc.applyEndOfTurnDamage(pokemon);
        if (dmg > 0) {
            TurnResult.ActionResult r = new TurnResult.ActionResult();
            r.actionType = "STATUS_DAMAGE";
            r.targetName = pokemon.name;
            r.damageDealt = dmg;
            r.message = pokemon.name + " is hurt by its " + pokemon.status + "!";
            results.add(r);
        }
    }

    private TurnResult buildTurnResult(BattleState state, BattlePokemon[] team1, BattlePokemon[] team2,
                                        List<TurnResult.ActionResult> actions) {
        TurnResult r = new TurnResult();
        r.turnNumber = state.turnNumber;
        r.actions = actions;
        r.teamP1 = team1;
        r.teamP2 = team2;
        r.activeSlot1 = state.activeSlot1;
        r.activeSlot2 = state.activeSlot2;
        return r;
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try { return objectMapper.readValue(json, type); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
}
