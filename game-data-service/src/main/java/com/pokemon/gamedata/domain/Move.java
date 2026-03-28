package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "moves")
public class Move {
    @Id
    private String id;
    private String name;
    private String type;
    private String category;
    private Integer basePower;
    private Integer accuracy;
    private int pp;
    private int priority;
    private SecondaryEffect secondaryEffect;

    @Data
    public static class SecondaryEffect {
        private int chance;
        private String effect;
    }
}
