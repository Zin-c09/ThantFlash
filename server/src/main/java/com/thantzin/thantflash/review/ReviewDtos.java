package com.thantzin.thantflash.review;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record ReviewRequest(@NotNull Grade grade) {
    }

    public record DailyCount(LocalDate date, long reviews) {
    }

    /**
     * @param accuracy share of answers in the last 7 days that were not AGAIN (0.0–1.0)
     * @param streakDays consecutive days (ending today or yesterday) with at least one review
     */
    public record StatsResponse(long totalCards, long dueNow, long reviewedToday, double accuracy,
            int streakDays, List<DailyCount> last7Days) {
    }
}
