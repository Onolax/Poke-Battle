package com.pokemon.battle.engine;

import com.pokemon.battle.dto.BattlePokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StatusEffectProcessorTest {

    StatusEffectProcessor processor;

    @BeforeEach
    void setUp() { processor = new StatusEffectProcessor(); }

    @Test
    void applyBrn_dealsDamageEachTurn() {
        BattlePokemon p = pokemon(160, List.of("Normal"));
        processor.applyStatus(p, "BRN");
        int dmg = processor.applyEndOfTurnDamage(p);
        assertThat(dmg).isEqualTo(10); // 160/16 = 10
        assertThat(p.currentHp).isEqualTo(150);
    }

    @Test
    void applyPsn_dealsDamageEachTurn() {
        BattlePokemon p = pokemon(160, List.of("Normal"));
        processor.applyStatus(p, "PSN");
        int dmg = processor.applyEndOfTurnDamage(p);
        assertThat(dmg).isEqualTo(20); // 160/8 = 20
    }

    @Test
    void fireType_immuneToBrn() {
        BattlePokemon p = pokemon(100, List.of("Fire"));
        boolean applied = processor.applyStatus(p, "BRN");
        assertThat(applied).isFalse();
        assertThat(p.status).isNull();
    }

    @Test
    void electricType_immuneToParalysis() {
        BattlePokemon p = pokemon(100, List.of("Electric"));
        boolean applied = processor.applyStatus(p, "PAR");
        assertThat(applied).isFalse();
    }

    @Test
    void poisonType_immuneToPoison() {
        BattlePokemon p = pokemon(100, List.of("Poison"));
        boolean applied = processor.applyStatus(p, "PSN");
        assertThat(applied).isFalse();
    }

    @Test
    void secondStatus_notApplied_whenAlreadyStatused() {
        BattlePokemon p = pokemon(100, List.of("Normal"));
        processor.applyStatus(p, "BRN");
        boolean second = processor.applyStatus(p, "PAR");
        assertThat(second).isFalse();
        assertThat(p.status).isEqualTo("BRN");
    }

    @Test
    void thawOnFireHit_clearsFrz() {
        BattlePokemon p = pokemon(100, List.of("Water"));
        processor.applyStatus(p, "FRZ");
        processor.thawOnFireHit(p, "Fire");
        assertThat(p.status).isNull();
    }

    @Test
    void brn_doesNotFaintPokemon_at1Hp_minDamageIs1() {
        BattlePokemon p = pokemon(16, List.of("Normal"));
        p.currentHp = 1;
        processor.applyStatus(p, "BRN");
        int dmg = processor.applyEndOfTurnDamage(p);
        assertThat(dmg).isEqualTo(1);
        assertThat(p.currentHp).isEqualTo(0);
        assertThat(p.fainted).isTrue();
    }

    private BattlePokemon pokemon(int hp, List<String> types) {
        BattlePokemon p = new BattlePokemon();
        p.maxHp = hp; p.currentHp = hp; p.types = types;
        p.statBoosts = Map.of();
        return p;
    }
}
