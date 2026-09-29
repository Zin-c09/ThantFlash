package com.thantflash.review;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StreakTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 29);

    @Test
    void countsConsecutiveDaysEndingToday() {
        assertThat(StatsService.streak(Set.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2), TODAY.minusDays(4)), TODAY))
                .isEqualTo(3);
    }

    @Test
    void streakSurvivesUntilUserStudiesToday() {
        assertThat(StatsService.streak(Set.of(TODAY.minusDays(1), TODAY.minusDays(2)), TODAY)).isEqualTo(2);
    }

    @Test
    void brokenStreakIsZero() {
        assertThat(StatsService.streak(Set.of(TODAY.minusDays(2)), TODAY)).isZero();
    }
}
