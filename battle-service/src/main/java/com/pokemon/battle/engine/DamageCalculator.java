package com.pokemon.battle.engine;

import com.pokemon.battle.dto.BattleMove;
import com.pokemon.battle.dto.BattlePokemon;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component
public class DamageCalculator {

    private static final int LEVEL = 50;
    private static final double STAB_MULTIPLIER = 1.5;
    private static final double CRIT_MULTIPLIER = 1.5;
    private static final double CRIT_CHANCE = 1.0 / 24.0;

    private final Random random = new Random();

    // Type chart: attackingType -> defendingType -> multiplier
    private static final Map<String, Map<String, Double>> TYPE_CHART = buildTypeChart();

    public int calculate(BattlePokemon attacker, BattlePokemon defender, BattleMove move) {
        if (move.basePower == 0) return 0; // Status move

        int atk = "Special".equals(move.category)
                ? getStat(attacker.spAtk, attacker.statBoosts.getOrDefault("spa", 0))
                : getStat(attacker.attack, attacker.statBoosts.getOrDefault("atk", 0));
        int def = "Special".equals(move.category)
                ? getStat(defender.spDef, defender.statBoosts.getOrDefault("spd", 0))
                : getStat(defender.defense, defender.statBoosts.getOrDefault("def", 0));

        // Apply BRN penalty on physical attack
        if ("Physical".equals(move.category) && "BRN".equals(attacker.status)) {
            atk = atk / 2;
        }

        // Base damage formula
        int baseDamage = (int) Math.floor(
                (Math.floor((Math.floor(2.0 * LEVEL / 5 + 2) * move.basePower * atk / def) / 50) + 2)
        );

        // Modifiers
        double stab = attacker.types.contains(move.type) ? STAB_MULTIPLIER : 1.0;
        double typeEff = getTypeEffectiveness(move.type, defender.types);
        boolean isCrit = random.nextDouble() < CRIT_CHANCE;
        double critMod = isCrit ? CRIT_MULTIPLIER : 1.0;
        double randomFactor = 0.85 + random.nextDouble() * 0.15;

        int finalDamage = (int) Math.floor(baseDamage * stab * typeEff * critMod * randomFactor);
        return Math.max(1, finalDamage); // min 1 damage if move hits
    }

    public boolean isCrit(BattleMove move) {
        return random.nextDouble() < CRIT_CHANCE;
    }

    public double getTypeEffectiveness(String moveType, java.util.List<String> defenderTypes) {
        double multiplier = 1.0;
        Map<String, Double> chart = TYPE_CHART.getOrDefault(moveType, Map.of());
        for (String defType : defenderTypes) {
            multiplier *= chart.getOrDefault(defType, 1.0);
        }
        return multiplier;
    }

    public String effectivenessLabel(double eff) {
        if (eff == 0) return "IMMUNE";
        if (eff > 1) return "SUPER";
        if (eff < 1) return "WEAK";
        return "NORMAL";
    }

    // Apply stat stage multiplier (stages -6 to +6)
    private int getStat(int base, int stage) {
        double[] multipliers = {0.25, 0.286, 0.333, 0.4, 0.5, 0.667, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0};
        int index = stage + 6;
        return (int) Math.floor(base * multipliers[Math.max(0, Math.min(12, index))]);
    }

    private static Map<String, Map<String, Double>> buildTypeChart() {
        return Map.ofEntries(
            Map.entry("Normal",   Map.of("Rock", 0.5, "Ghost", 0.0, "Steel", 0.5)),
            Map.entry("Fire",     Map.of("Fire", 0.5, "Water", 0.5, "Grass", 2.0, "Ice", 2.0,
                                         "Bug", 2.0, "Rock", 0.5, "Dragon", 0.5, "Steel", 2.0)),
            Map.entry("Water",    Map.of("Fire", 2.0, "Water", 0.5, "Grass", 0.5, "Ground", 2.0,
                                         "Rock", 2.0, "Dragon", 0.5)),
            Map.entry("Electric", Map.of("Water", 2.0, "Electric", 0.5, "Grass", 0.5, "Ground", 0.0,
                                         "Flying", 2.0, "Dragon", 0.5)),
            Map.entry("Grass",    Map.of("Fire", 0.5, "Water", 2.0, "Grass", 0.5, "Poison", 0.5,
                                         "Ground", 2.0, "Flying", 0.5, "Bug", 0.5, "Rock", 2.0,
                                         "Dragon", 0.5, "Steel", 0.5)),
            Map.entry("Ice",      Map.of("Fire", 0.5, "Water", 0.5, "Grass", 2.0, "Ice", 0.5,
                                         "Ground", 2.0, "Flying", 2.0, "Dragon", 2.0, "Steel", 0.5)),
            Map.entry("Fighting", java.util.Map.ofEntries(
                                         Map.entry("Normal", 2.0), Map.entry("Ice", 2.0),
                                         Map.entry("Poison", 0.5), Map.entry("Flying", 0.5),
                                         Map.entry("Psychic", 0.5), Map.entry("Bug", 0.5),
                                         Map.entry("Rock", 2.0), Map.entry("Ghost", 0.0),
                                         Map.entry("Dark", 2.0), Map.entry("Steel", 2.0),
                                         Map.entry("Fairy", 0.5))),
            Map.entry("Poison",   Map.of("Grass", 2.0, "Poison", 0.5, "Ground", 0.5, "Rock", 0.5,
                                         "Ghost", 0.5, "Steel", 0.0, "Fairy", 2.0)),
            Map.entry("Ground",   Map.of("Fire", 2.0, "Electric", 2.0, "Grass", 0.5, "Poison", 2.0,
                                         "Flying", 0.0, "Bug", 0.5, "Rock", 2.0, "Steel", 2.0)),
            Map.entry("Flying",   Map.of("Electric", 0.5, "Grass", 2.0, "Fighting", 2.0, "Bug", 2.0,
                                         "Rock", 0.5, "Steel", 0.5)),
            Map.entry("Psychic",  Map.of("Fighting", 2.0, "Poison", 2.0, "Psychic", 0.5,
                                         "Dark", 0.0, "Steel", 0.5)),
            Map.entry("Bug",      Map.of("Fire", 0.5, "Grass", 2.0, "Fighting", 0.5, "Flying", 0.5,
                                         "Psychic", 2.0, "Ghost", 0.5, "Dark", 2.0, "Steel", 0.5,
                                         "Fairy", 0.5)),
            Map.entry("Rock",     Map.of("Fire", 2.0, "Ice", 2.0, "Fighting", 0.5, "Ground", 0.5,
                                         "Flying", 2.0, "Bug", 2.0, "Steel", 0.5)),
            Map.entry("Ghost",    Map.of("Normal", 0.0, "Psychic", 2.0, "Ghost", 2.0, "Dark", 0.5)),
            Map.entry("Dragon",   Map.of("Dragon", 2.0, "Steel", 0.5, "Fairy", 0.0)),
            Map.entry("Dark",     Map.of("Fighting", 0.5, "Psychic", 2.0, "Ghost", 2.0,
                                         "Dark", 0.5, "Fairy", 0.5)),
            Map.entry("Steel",    Map.of("Fire", 0.5, "Water", 0.5, "Electric", 0.5, "Ice", 2.0,
                                         "Rock", 2.0, "Steel", 0.5, "Fairy", 2.0)),
            Map.entry("Fairy",    Map.of("Fire", 0.5, "Fighting", 2.0, "Poison", 0.5, "Dragon", 2.0,
                                         "Dark", 2.0, "Steel", 0.5))
        );
    }
}
