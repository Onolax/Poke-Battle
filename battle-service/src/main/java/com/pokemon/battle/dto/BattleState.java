package com.pokemon.battle.dto;

public class BattleState {
    public String battleId;
    public String phase;          // ACTIVE | ENDED
    public int turnNumber;
    public String player1Id;
    public String player1Username;
    public String player2Id;
    public String player2Username;
    public int activeSlot1;
    public int activeSlot2;
    public String weather;        // null | SUN | RAIN | SAND | SNOW
    public String winnerId;
    public String endReason;      // NORMAL | FORFEIT | DISCONNECT

    public BattleState() {}
}
