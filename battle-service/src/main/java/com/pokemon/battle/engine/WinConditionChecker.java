package com.pokemon.battle.engine;

import com.pokemon.battle.dto.BattlePokemon;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class WinConditionChecker {

    public boolean isTeamDefeated(BattlePokemon[] team) {
        return Arrays.stream(team).allMatch(p -> p.fainted);
    }

    public boolean isBattleOver(BattlePokemon[] team1, BattlePokemon[] team2) {
        return isTeamDefeated(team1) || isTeamDefeated(team2);
    }
}
