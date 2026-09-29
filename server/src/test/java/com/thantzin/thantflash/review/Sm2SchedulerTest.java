package com.thantzin.thantflash.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class Sm2SchedulerTest {

    private static final Instant NOW = Instant.parse("2026-04-01T09:00:00Z");
    private static final Schedule NEW_CARD = new Schedule(0, 2.5, NOW);

    private final Sm2Scheduler scheduler = new Sm2Scheduler();

    @Test
    void newCardGoodIsDueTomorrow() {
        Schedule s = scheduler.next(NEW_CARD, Grade.GOOD, NOW);
        assertThat(s.intervalDays()).isEqualTo(1);
        assertThat(s.ease()).isEqualTo(2.5);
        assertThat(s.dueAt()).isEqualTo(NOW.plus(Duration.ofDays(1)));
    }

    @Test
    void newCardEasyIsDueInThreeDaysAndEaseGrows() {
        Schedule s = scheduler.next(NEW_CARD, Grade.EASY, NOW);
        assertThat(s.intervalDays()).isEqualTo(3);
        assertThat(s.ease()).isCloseTo(2.65, within(1e-9));
    }

    @Test
    void goodMultipliesIntervalByEase() {
        Schedule s = scheduler.next(new Schedule(4, 2.5, NOW), Grade.GOOD, NOW);
        assertThat(s.intervalDays()).isEqualTo(10);
    }

    @Test
    void againResetsIntervalAndRepeatsInOneMinute() {
        Schedule s = scheduler.next(new Schedule(10, 2.5, NOW), Grade.AGAIN, NOW);
        assertThat(s.intervalDays()).isZero();
        assertThat(s.ease()).isCloseTo(2.3, within(1e-9));
        assertThat(s.dueAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void hardIsAtLeastOneDay() {
        Schedule s = scheduler.next(NEW_CARD, Grade.HARD, NOW);
        assertThat(s.intervalDays()).isEqualTo(1);
        assertThat(s.ease()).isCloseTo(2.35, within(1e-9));
    }

    @Test
    void easeNeverDropsBelowMinimum() {
        Schedule s = new Schedule(1, Schedule.MIN_EASE, NOW);
        assertThat(scheduler.next(s, Grade.AGAIN, NOW).ease()).isEqualTo(Schedule.MIN_EASE);
        assertThat(scheduler.next(s, Grade.HARD, NOW).ease()).isEqualTo(Schedule.MIN_EASE);
    }

    @Test
    void intervalIsRoundedToOneDecimal() {
        Schedule s = scheduler.next(new Schedule(1.3, 2.5, NOW), Grade.GOOD, NOW); // 3.25 → 3.3
        assertThat(s.intervalDays()).isEqualTo(3.3);
    }
}
