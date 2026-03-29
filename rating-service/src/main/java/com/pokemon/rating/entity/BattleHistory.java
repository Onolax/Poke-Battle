package com.pokemon.rating.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "battle_history")
public class BattleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "player1_id") private UUID player1Id;
    @Column(name = "player2_id") private UUID player2Id;
    @Column(name = "winner_id")  private UUID winnerId;
    @Column(name = "format", nullable = false) private String format;
    @Column(name = "started_at") private Instant startedAt = Instant.now();
    @Column(name = "ended_at")   private Instant endedAt = Instant.now();
    @Column(name = "end_reason") private String endReason;

    public BattleHistory() {}

    public static BattleHistory of(UUID winnerId, UUID loserId, String format, String reason) {
        BattleHistory h = new BattleHistory();
        h.player1Id = winnerId;
        h.player2Id = loserId;
        h.winnerId = winnerId;
        h.format = format;
        h.endReason = reason;
        return h;
    }
}
