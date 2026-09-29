package com.thantflash.card;

import static org.assertj.core.api.Assertions.assertThat;

import com.thantflash.card.Sm2Scheduler.State;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class Sm2SchedulerTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final State NEW_CARD = new State(0, 2.5, 0, NOW);

    @Test
    void goodOnNewCardSchedulesOneDay() {
        State s = Sm2Scheduler.next(NEW_CARD, Grade.GOOD, NOW);
        assertThat(s.intervalDays()).isEqualTo(1);
        assertThat(s.due()).isEqualTo(NOW.plus(Duration.ofDays(1)));
        assertThat(s.reps()).isEqualTo(1);
        assertThat(s.ease()).isEqualTo(2.5);
    }

    @Test
    void goodMultipliesIntervalByEase() {
        State s = Sm2Scheduler.next(new State(4, 2.5, 3, NOW), Grade.GOOD, NOW);
        assertThat(s.intervalDays()).isEqualTo(10);
    }

    @Test
    void easyOnNewCardSchedulesThreeDaysAndRaisesEase() {
        State s = Sm2Scheduler.next(NEW_CARD, Grade.EASY, NOW);
        assertThat(s.intervalDays()).isEqualTo(3);
        assertThat(s.ease()).isEqualTo(2.65);
    }

    @Test
    void hardGrowsSlowlyAndLowersEase() {
        State s = Sm2Scheduler.next(new State(10, 2.5, 3, NOW), Grade.HARD, NOW);
        assertThat(s.intervalDays()).isEqualTo(12);
        assertThat(s.ease()).isEqualTo(2.35);
    }

    @Test
    void againResetsIntervalAndIsDueWithinSession() {
        State s = Sm2Scheduler.next(new State(20, 2.5, 5, NOW), Grade.AGAIN, NOW);
        assertThat(s.intervalDays()).isZero();
        assertThat(s.ease()).isEqualTo(2.3);
        assertThat(s.due()).isEqualTo(NOW.plus(Sm2Scheduler.RELEARN_DELAY));
    }

    @Test
    void easeNeverDropsBelowMinimum() {
        State s = new State(5, 1.35, 1, NOW);
        for (int i = 0; i < 10; i++) s = Sm2Scheduler.next(s, Grade.AGAIN, NOW);
        assertThat(s.ease()).isEqualTo(Sm2Scheduler.MIN_EASE);
    }
}
