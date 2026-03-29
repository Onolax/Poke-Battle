package com.pokemon.rating.repository;

import com.pokemon.rating.entity.Rating;
import com.pokemon.rating.entity.RatingId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Rating r WHERE r.userId = :userId AND r.format = :format")
    Optional<Rating> findByUserIdAndFormatForUpdate(UUID userId, String format);

    Optional<Rating> findByUserIdAndFormat(UUID userId, String format);

    @Query("SELECT r FROM Rating r WHERE r.format = :format ORDER BY r.elo DESC")
    List<Rating> findTopByFormatOrderByEloDesc(String format, org.springframework.data.domain.Pageable pageable);
}
