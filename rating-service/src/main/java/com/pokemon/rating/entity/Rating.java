package com.pokemon.rating.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ratings")
@IdClass(RatingId.class)
public class Rating {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "format")
    private String format;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "elo", nullable = false)
    private int elo = 1000;

    @Column(name = "wins", nullable = false)
    private int wins = 0;

    @Column(name = "losses", nullable = false)
    private int losses = 0;

    public Rating() {}

    public Rating(UUID userId, String username, String format) {
        this.userId = userId;
        this.username = username;
        this.format = format;
        this.elo = 1000;
    }

    public UUID getUserId() { return userId; }
    public String getFormat() { return format; }
    public String getUsername() { return username; }
    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
}
