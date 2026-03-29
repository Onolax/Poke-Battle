package com.pokemon.battle.dto;

import java.util.List;

public class TurnResult {
    public String type = "TURN_RESULT";
    public int turnNumber;
    public List<ActionResult> actions;
    public BattlePokemon[] teamP1;
    public BattlePokemon[] teamP2;
    public int activeSlot1;
    public int activeSlot2;

    public TurnResult() {}

    public static class ActionResult {
        public String playerId;
        public String actionType;   // MOVE | SWITCH | FORFEIT
        public String moveName;
        public String targetName;
        public int damageDealt;
        public boolean crit;
        public String effectiveness; // SUPER | WEAK | IMMUNE | NORMAL
        public String statusApplied;
        public String message;

        public ActionResult() {}
    }
}
