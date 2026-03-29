package com.pokemon.rating.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EloCalculatorTest {

    EloCalculator calculator;

    @BeforeEach
    void setUp() { calculator = new EloCalculator(); }

    @Test
    void equalElo_winnerGains16_loserLoses16() {
        var result = calculator.calculate(1000, 1000);
        assertThat(result.newWinnerElo()).isEqualTo(1016);
        assertThat(result.newLoserElo()).isEqualTo(984);
        assertThat(result.delta()).isEqualTo(16);
    }

    @Test
    void higherEloWinner_gainsLess() {
        var result = calculator.calculate(1200, 1000);
        assertThat(result.delta()).isLessThan(16);
    }

    @Test
    void lowerEloWinner_gainsMore() {
        var result = calculator.calculate(1000, 1200);
        assertThat(result.delta()).isGreaterThan(16);
    }

    @Test
    void eloNeverFallsBelowFloor() {
        var result = calculator.calculate(150, 2000);
        assertThat(result.newLoserElo()).isGreaterThanOrEqualTo(100);
    }

    @Test
    void sumOfEloIsConserved_approximately() {
        int total = 2000;
        var result = calculator.calculate(1000, 1000);
        int newTotal = result.newWinnerElo() + result.newLoserElo();
        assertThat(newTotal).isEqualTo(total); // K*1 + K*(-1) = 0 net change when equal
    }
}
