package com.thantzin.thantflash.review;

import java.time.Instant;

/** Spaced-repetition state of one card. */
public record Schedule(double intervalDays, double ease, Instant dueAt) {

    public static final double INITIAL_EASE = 2.5;
    public static final double MIN_EASE = 1.3;
}
