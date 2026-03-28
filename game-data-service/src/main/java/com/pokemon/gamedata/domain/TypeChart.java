package com.pokemon.gamedata.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "type_charts")
public class TypeChart {
    @Id
    private String id;
    private Map<String, Map<String, Double>> chart;
}
