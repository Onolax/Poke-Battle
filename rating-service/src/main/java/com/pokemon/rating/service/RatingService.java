package com.pokemon.rating.service;

import com.pokemon.rating.dto.BattleEndedEvent;
import com.pokemon.rating.dto.LeaderboardEntry;
import com.pokemon.rating.dto.RatingResponse;
import com.pokemon.rating.entity.BattleHistory;
import com.pokemon.rating.entity.Rating;
import com.pokemon.rating.repository.BattleHistoryRepository;
import com.pokemon.rating.repository.RatingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final BattleHistoryRepository historyRepository;
    private final EloCalculator eloCalculator;

    public RatingService(RatingRepository ratingRepository,
                          BattleHistoryRepository historyRepository,
                          EloCalculator eloCalculator) {
        this.ratingRepository = ratingRepository;
        this.historyRepository = historyRepository;
        this.eloCalculator = eloCalculator;
    }

    @Transactional
    public void processBattleResult(BattleEndedEvent event) {
        UUID winnerId = UUID.fromString(event.winnerId());
        UUID loserId  = UUID.fromString(event.loserId());

        // Fetch or create rating rows with pessimistic lock
        Rating winnerRating = ratingRepository.findByUserIdAndFormatForUpdate(winnerId, event.format())
                .orElseGet(() -> ratingRepository.save(new Rating(winnerId, event.winnerUsername(), event.format())));
        Rating loserRating = ratingRepository.findByUserIdAndFormatForUpdate(loserId, event.format())
                .orElseGet(() -> ratingRepository.save(new Rating(loserId, event.loserUsername(), event.format())));

        EloCalculator.EloResult result = eloCalculator.calculate(winnerRating.getElo(), loserRating.getElo());

        winnerRating.setElo(result.newWinnerElo());
        winnerRating.setWins(winnerRating.getWins() + 1);
        loserRating.setElo(result.newLoserElo());
        loserRating.setLosses(loserRating.getLosses() + 1);

        ratingRepository.save(winnerRating);
        ratingRepository.save(loserRating);
        historyRepository.save(BattleHistory.of(winnerId, loserId, event.format(), "NORMAL"));
    }

    public RatingResponse getRating(UUID userId, String format) {
        return ratingRepository.findByUserIdAndFormat(userId, format)
                .map(r -> new RatingResponse(r.getUserId(), r.getUsername(), r.getFormat(),
                        r.getElo(), r.getWins(), r.getLosses()))
                .orElseGet(() -> new RatingResponse(userId, "unknown", format, 1000, 0, 0));
    }

    public List<LeaderboardEntry> getLeaderboard(String format) {
        List<Rating> top = ratingRepository.findTopByFormatOrderByEloDesc(format, PageRequest.of(0, 10));
        AtomicInteger rank = new AtomicInteger(1);
        return top.stream()
                .map(r -> new LeaderboardEntry(rank.getAndIncrement(), r.getUserId(),
                        r.getUsername(), r.getElo(), r.getWins(), r.getLosses()))
                .toList();
    }
}
