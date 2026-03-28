package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Move;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MoveRepository extends MongoRepository<Move, String> {
    List<Move> findByType(String type);
    List<Move> findByCategory(String category);
}
