package com.thantzin.thantflash.review;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

/**
 * Simplified SM-2 algorithm — the same rules as grade() in the web app (app.js):
 * <ul>
 *   <li>AGAIN: interval resets to 0, ease −0.2, due again in 1 minute</li>
 *   <li>HARD : interval ×1.2 (at least 1 day), ease −0.15</li>
 *   <li>GOOD : interval × ease (first time: 1 day)</li>
 *   <li>EASY : interval × ease × 1.3 (first time: 3 days), ease +0.15</li>
 * </ul>
 * Ease never goes below 1.3. Pure function: no DB, no clock — easy to unit test.
 */
@Component
public class Sm2Scheduler {

    static final Duration AGAIN_DELAY = Duration.ofMinutes(1);

    public Schedule next(Schedule s, Grade grade, Instant now) {
        double interval = s.intervalDays();
        double ease = s.ease();

        switch (grade) {
            case AGAIN -> {
                return new Schedule(0, Math.max(Schedule.MIN_EASE, ease - 0.2), now.plus(AGAIN_DELAY));
            }
            case HARD -> {
                interval = Math.max(1, interval * 1.2);
                ease = Math.max(Schedule.MIN_EASE, ease - 0.15);
            }
            case GOOD -> interval = interval > 0 ? interval * ease : 1;
            case EASY -> {
                interval = interval > 0 ? interval * ease * 1.3 : 3;
                ease += 0.15;
            }
        }
        interval = Math.round(interval * 10) / 10.0;
        long seconds = Math.round(interval * Duration.ofDays(1).toSeconds());
        return new Schedule(interval, ease, now.plusSeconds(seconds));
    }
}
