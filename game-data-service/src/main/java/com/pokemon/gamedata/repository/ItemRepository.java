package com.pokemon.gamedata.repository;

import com.pokemon.gamedata.domain.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Item, String> {
}
