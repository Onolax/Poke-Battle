package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Format;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FormatRepository extends MongoRepository<Format, String> {
}
