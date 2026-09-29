package com.thantflash.card;

import java.time.Duration;
import java.time.Instant;

/**
 * Simplified SM-2 spaced-repetition scheduling. This is a pure function of
 * (current state, grade, now) so it is trivially unit-testable, and it mirrors
 * the rules in the browser app ({@code app.js#grade}) so both stay in sync.
 */
public final class Sm2Scheduler {

    static final double MIN_EASE = 1.3;
    /** "Again" makes the card due again almost immediately, within the same session. */
    static final Duration RELEARN_DELAY = Duration.ofMinutes(1);

    private Sm2Scheduler() {
    }

    public record State(double intervalDays, double ease, int reps, Instant due) {
    }

    public static State next(State s, Grade grade, Instant now) {
        double interval = s.intervalDays();
        double ease = s.ease();
        if (grade == Grade.AGAIN) {
            return new State(0, Math.max(MIN_EASE, ease - 0.2), s.reps() + 1, now.plus(RELEARN_DELAY));
        }
        switch (grade) {
            case HARD -> {
                interval = Math.max(1, interval * 1.2);
                ease = Math.max(MIN_EASE, ease - 0.15);
            }
            case GOOD -> interval = interval > 0 ? interval * ease : 1;
            case EASY -> {
                interval = interval > 0 ? interval * ease * 1.3 : 3;
                ease += 0.15;
            }
            default -> throw new IllegalStateException();
        }
        interval = Math.round(interval * 10) / 10.0;
        Instant due = now.plus(Duration.ofSeconds(Math.round(interval * 86_400)));
        return new State(interval, ease, s.reps() + 1, due);
    }
}
