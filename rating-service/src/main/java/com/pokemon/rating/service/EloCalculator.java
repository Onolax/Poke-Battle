package com.pokemon.rating.service;

import org.springframework.stereotype.Component;

@Component
public class EloCalculator {

    private static final int K = 32;

    public record EloResult(int newWinnerElo, int newLoserElo, int delta) {}

    public EloResult calculate(int winnerElo, int loserElo) {
        double expectedWinner = 1.0 / (1 + Math.pow(10, (loserElo - winnerElo) / 400.0));
        double expectedLoser = 1.0 - expectedWinner;

        int winnerDelta = (int) Math.round(K * (1.0 - expectedWinner));
        int loserDelta  = (int) Math.round(K * (0.0 - expectedLoser));

        return new EloResult(
                Math.max(100, winnerElo + winnerDelta),
                Math.max(100, loserElo + loserDelta),
                winnerDelta
        );
    }
}
