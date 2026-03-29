package com.pokemon.rating.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class RatingId implements Serializable {
    private UUID userId;
    private String format;

    public RatingId() {}
    public RatingId(UUID userId, String format) { this.userId = userId; this.format = format; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RatingId r)) return false;
        return Objects.equals(userId, r.userId) && Objects.equals(format, r.format);
    }
    @Override public int hashCode() { return Objects.hash(userId, format); }
}
