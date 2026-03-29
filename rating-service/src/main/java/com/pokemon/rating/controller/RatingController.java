package com.pokemon.rating.controller;

import com.pokemon.rating.dto.LeaderboardEntry;
import com.pokemon.rating.dto.RatingResponse;
import com.pokemon.rating.service.RatingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/{userId}/{format}")
    public RatingResponse getRating(@PathVariable UUID userId, @PathVariable String format) {
        return ratingService.getRating(userId, format);
    }

    @GetMapping("/leaderboard/{format}")
    public List<LeaderboardEntry> getLeaderboard(@PathVariable String format) {
        return ratingService.getLeaderboard(format);
    }
}
