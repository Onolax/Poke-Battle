package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "pokemon")
public class Pokemon {
    @Id
    private String id;
    private int dexNumber;
    private String name;
    private List<String> types;
    private BaseStats baseStats;
    private List<String> abilities;
    private List<String> learnset;
    private Map<String, String> tier;
    private double weightKg;
}
