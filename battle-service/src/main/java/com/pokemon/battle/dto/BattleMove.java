package com.pokemon.battle.dto;

public class BattleMove {
    public String slug;
    public String name;
    public String type;
    public String category;   // Physical | Special | Status
    public int basePower;
    public int accuracy;
    public int pp;
    public int currentPp;
    public int priority;
    public SecondaryEffect secondaryEffect;

    public BattleMove() {}

    public static class SecondaryEffect {
        public int chance;
        public String effect; // BURN | PAR | PSN | SLP | FRZ | STAT_BOOST
        public String stat;
        public int stages;

        public SecondaryEffect() {}
    }
}
