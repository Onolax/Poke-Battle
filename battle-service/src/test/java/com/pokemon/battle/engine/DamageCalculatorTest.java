package com.pokemon.battle.engine;

import com.pokemon.battle.dto.BattleMove;
import com.pokemon.battle.dto.BattlePokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DamageCalculatorTest {

    DamageCalculator calculator;

    @BeforeEach
    void setUp() { calculator = new DamageCalculator(); }

    @Test
    void normalDamage_isPositive() {
        BattlePokemon attacker = pokemonWith(80, 70, List.of("Fire"));
        BattlePokemon defender = pokemonWith(70, 80, List.of("Normal"));
        BattleMove move = physicalMove("tackle", "Normal", 40);

        int damage = calculator.calculate(attacker, defender, move);
        assertThat(damage).isGreaterThan(0);
    }

    @Test
    void superEffective_dealMoreDamage() {
        BattlePokemon attacker = pokemonWith(80, 70, List.of("Water"));
        BattlePokemon defender = pokemonWith(70, 80, List.of("Fire"));
        BattleMove move = specialMove("surf", "Water", 90);

        BattlePokemon attacker2 = pokemonWith(80, 70, List.of("Normal"));
        BattleMove normalMove = specialMove("swift", "Normal", 90);

        int superDmg = calculator.calculate(attacker, defender, move);
        int normalDmg = calculator.calculate(attacker2, defender, normalMove);
        assertThat(superDmg).isGreaterThan(normalDmg);
    }

    @Test
    void immuneType_dealZeroDamage() {
        BattlePokemon attacker = pokemonWith(80, 70, List.of("Normal"));
        BattlePokemon defender = pokemonWith(70, 80, List.of("Ghost"));
        BattleMove move = physicalMove("tackle", "Normal", 40);

        double eff = calculator.getTypeEffectiveness("Normal", List.of("Ghost"));
        assertThat(eff).isEqualTo(0.0);
    }

    @Test
    void stab_appliesOnePointFiveMultiplier() {
        BattlePokemon stabAttacker = pokemonWith(80, 70, List.of("Fire"));
        BattlePokemon noStabAttacker = pokemonWith(80, 70, List.of("Water"));
        BattlePokemon defender = pokemonWith(70, 80, List.of("Normal"));
        BattleMove fireMove = specialMove("ember", "Fire", 40);

        int stabDmg = calculator.calculate(stabAttacker, defender, fireMove);
        int noStabDmg = calculator.calculate(noStabAttacker, defender, fireMove);
        assertThat(stabDmg).isGreaterThan(noStabDmg);
    }

    @Test
    void brnHalvesPhysicalAttack() {
        BattlePokemon attacker = pokemonWith(100, 70, List.of("Normal"));
        BattlePokemon defender = pokemonWith(70, 80, List.of("Normal"));
        BattleMove move = physicalMove("tackle", "Normal", 80);

        int normalDmg = calculator.calculate(attacker, defender, move);
        attacker.status = "BRN";
        int brnDmg = calculator.calculate(attacker, defender, move);
        assertThat(brnDmg).isLessThan(normalDmg);
    }

    @Test
    void statusMove_dealNoDamage() {
        BattlePokemon attacker = pokemonWith(80, 70, List.of("Normal"));
        BattlePokemon defender = pokemonWith(70, 80, List.of("Normal"));
        BattleMove move = physicalMove("growl", "Normal", 0);
        assertThat(calculator.calculate(attacker, defender, move)).isEqualTo(0);
    }

    private BattlePokemon pokemonWith(int atk, int def, List<String> types) {
        BattlePokemon p = new BattlePokemon();
        p.attack = atk; p.defense = def;
        p.spAtk = atk; p.spDef = def;
        p.types = types; p.statBoosts = Map.of();
        return p;
    }

    private BattleMove physicalMove(String slug, String type, int power) {
        BattleMove m = new BattleMove();
        m.slug = slug; m.type = type; m.category = "Physical"; m.basePower = power;
        return m;
    }

    private BattleMove specialMove(String slug, String type, int power) {
        BattleMove m = new BattleMove();
        m.slug = slug; m.type = type; m.category = "Special"; m.basePower = power;
        return m;
    }
}
