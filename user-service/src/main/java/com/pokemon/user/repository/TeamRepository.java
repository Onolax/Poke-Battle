package com.pokemon.user.repository;

import com.pokemon.user.domain.Team;
import com.pokemon.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findAllByUser(User user);
    Optional<Team> findByIdAndUser(UUID id, User user);
}
