package com.thantflash.reminder;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class RepeatTest {

    private static final ZoneId YANGON = ZoneId.of("Asia/Yangon");

    @Test
    void dailyRollsToNextFutureOccurrenceSkippingMissedDays() {
        Instant fireAt = Instant.parse("2026-01-01T01:30:00Z"); // 08:00 Yangon
        Instant now = Instant.parse("2026-01-03T12:00:00Z");
        assertThat(Repeat.DAILY.nextAfter(fireAt, now, YANGON)).isEqualTo(Instant.parse("2026-01-04T01:30:00Z"));
    }

    @Test
    void weeklyKeepsLocalWallClockAcrossDst() {
        ZoneId ny = ZoneId.of("America/New_York");
        Instant fireAt = Instant.parse("2026-03-02T13:00:00Z"); // Mon 08:00 EST
        Instant now = Instant.parse("2026-03-03T00:00:00Z");
        // After the 8 March DST switch, 08:00 EDT is 12:00Z.
        assertThat(Repeat.WEEKLY.nextAfter(fireAt, now.plusSeconds(7 * 86_400), ny))
                .isEqualTo(Instant.parse("2026-03-16T12:00:00Z"));
    }
}
