package com.thantflash.reminder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Instant;

/** Same options as the web app: none / daily / weekly. */
public enum Repeat {
    NONE, DAILY, WEEKLY;

    /**
     * Next occurrence strictly after {@code now}, stepping in the owner's zone
     * so a "daily 8:00" reminder stays at 8:00 local time.
     */
    public Instant nextAfter(Instant fireAt, Instant now, ZoneId zone) {
        if (this == NONE) throw new IllegalStateException("NONE does not repeat");
        ZonedDateTime t = fireAt.atZone(zone);
        while (!t.toInstant().isAfter(now)) {
            t = this == DAILY ? t.plusDays(1) : t.plusWeeks(1);
        }
        return t.toInstant();
    }
}
