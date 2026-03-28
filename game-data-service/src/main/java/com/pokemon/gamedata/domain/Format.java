package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "formats")
public class Format {
    @Id
    private String id;
    private String name;
    private int generation;
    private List<String> bannedPokemon;
    private List<String> bannedMoves;
    private int teamSize;
    private int activeSize;
}
