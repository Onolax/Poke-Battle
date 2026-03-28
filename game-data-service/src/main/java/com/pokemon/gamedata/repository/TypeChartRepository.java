package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.TypeChart;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TypeChartRepository extends MongoRepository<TypeChart, String> {
}
