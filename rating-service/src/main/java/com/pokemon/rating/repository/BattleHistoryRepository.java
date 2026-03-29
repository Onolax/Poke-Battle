package com.pokemon.rating.repository;

import com.pokemon.rating.entity.BattleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BattleHistoryRepository extends JpaRepository<BattleHistory, UUID> {}
