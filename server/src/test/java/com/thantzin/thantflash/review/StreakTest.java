package com.thantzin.thantflash.review;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

class StreakTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 4, 10);

    @Test
    void countsConsecutiveDaysIncludingToday() {
        Set<LocalDate> days = Set.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2), TODAY.minusDays(4));
        assertThat(ReviewService.streak(days::contains, TODAY)).isEqualTo(3);
    }

    @Test
    void notStudiedYetTodayKeepsYesterdaysStreak() {
        Set<LocalDate> days = Set.of(TODAY.minusDays(1), TODAY.minusDays(2));
        assertThat(ReviewService.streak(days::contains, TODAY)).isEqualTo(2);
    }

    @Test
    void zeroWhenNothingRecent() {
        assertThat(ReviewService.streak(Set.of(TODAY.minusDays(3))::contains, TODAY)).isZero();
    }
}
