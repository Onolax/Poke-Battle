package com.pokemon.battle.engine;

import com.pokemon.battle.dto.BattlePokemon;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class StatusEffectProcessor {

    private final Random random = new Random();

    /**
     * Returns true if the Pokemon can act this turn (not fully paralysed/asleep/frozen).
     */
    public boolean canAct(BattlePokemon pokemon) {
        if (pokemon.status == null) return true;
        return switch (pokemon.status) {
            case "PAR" -> random.nextDouble() >= 0.25; // 25% skip
            case "SLP" -> {
                if (pokemon.statusTurns <= 0) {
                    pokemon.status = null; // wake up
                    yield true;
                }
                pokemon.statusTurns--;
                yield false;
            }
            case "FRZ" -> {
                if (random.nextDouble() < 0.20) {
                    pokemon.status = null; // thaw
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    /**
     * Apply end-of-turn status damage (BRN, PSN). Returns HP lost.
     */
    public int applyEndOfTurnDamage(BattlePokemon pokemon) {
        if (pokemon.status == null || pokemon.fainted) return 0;
        return switch (pokemon.status) {
            case "BRN" -> applyDot(pokemon, Math.max(1, pokemon.maxHp / 16));
            case "PSN" -> applyDot(pokemon, Math.max(1, pokemon.maxHp / 8));
            default -> 0;
        };
    }

    /**
     * Attempt to apply a status condition. Returns true if applied.
     * A Pokemon already with a status cannot receive another.
     */
    public boolean applyStatus(BattlePokemon pokemon, String status) {
        if (pokemon.status != null || pokemon.fainted) return false;
        // Type immunities
        if ("BRN".equals(status) && pokemon.types.contains("Fire")) return false;
        if ("FRZ".equals(status) && pokemon.types.contains("Ice")) return false;
        if ("PAR".equals(status) && pokemon.types.contains("Electric")) return false;
        if (("PSN".equals(status)) && (pokemon.types.contains("Poison") || pokemon.types.contains("Steel"))) return false;

        pokemon.status = status;
        if ("SLP".equals(status)) {
            pokemon.statusTurns = 1 + random.nextInt(3); // 1-3 turns
        }
        return true;
    }

    /**
     * Thaw a frozen Pokemon when hit by a Fire move.
     */
    public void thawOnFireHit(BattlePokemon defender, String moveType) {
        if ("FRZ".equals(defender.status) && "Fire".equals(moveType)) {
            defender.status = null;
        }
    }

    private int applyDot(BattlePokemon pokemon, int damage) {
        pokemon.currentHp = Math.max(0, pokemon.currentHp - damage);
        if (pokemon.currentHp == 0) pokemon.fainted = true;
        return damage;
    }
}
