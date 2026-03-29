package com.pokemon.battle.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattlePokemon {
    public String slug;
    public String name;
    public int dexNumber;
    public List<String> types;
    public int maxHp;
    public int currentHp;
    public int attack;
    public int defense;
    public int spAtk;
    public int spDef;
    public int speed;
    public String status;        // null | BRN | PSN | PAR | SLP | FRZ
    public int statusTurns;      // remaining turns for SLP/FRZ
    public Map<String, Integer> statBoosts = new HashMap<>(); // atk/def/spa/spd/spe → -6..+6
    public List<BattleMove> moves;
    public boolean fainted;

    public BattlePokemon() {}
}
