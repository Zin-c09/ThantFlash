package com.thantflash.review;

import com.thantflash.card.Grade;
import com.thantflash.common.NotFoundException;
import com.thantflash.user.User;
import com.thantflash.user.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Study statistics computed in the user's own time zone, so a review at
 * 23:30 in Yangon counts for that local day rather than the UTC one.
 */
@Service
public class StatsService {

    static final int HISTORY_DAYS = 365;

    private final ReviewLogRepository reviewLogs;
    private final UserRepository users;
    private final Clock clock;

    public StatsService(ReviewLogRepository reviewLogs, UserRepository users, Clock clock) {
        this.reviewLogs = reviewLogs;
        this.users = users;
        this.clock = clock;
    }

    public record DayCount(LocalDate date, long reviews) {
    }

    public record Stats(long reviewsToday, int currentStreak, double retentionRate30d, List<DayCount> last30Days) {
    }

    @Transactional(readOnly = true)
    public Stats stats(long userId) {
        User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User", userId));
        ZoneId zone = ZoneId.of(user.getTimeZone());
        LocalDate today = LocalDate.now(clock.withZone(zone));
        List<ReviewLog> logs = reviewLogs.findSince(userId, clock.instant().minus(Duration.ofDays(HISTORY_DAYS + 1)));

        Map<LocalDate, Long> perDay = logs.stream().collect(Collectors.groupingBy(
                l -> LocalDate.ofInstant(l.getReviewedAt(), zone), TreeMap::new, Collectors.counting()));

        List<DayCount> last30 = new ArrayList<>();
        for (LocalDate d = today.minusDays(29); !d.isAfter(today); d = d.plusDays(1)) {
            last30.add(new DayCount(d, perDay.getOrDefault(d, 0L)));
        }

        List<ReviewLog> recent = logs.stream()
                .filter(l -> !LocalDate.ofInstant(l.getReviewedAt(), zone).isBefore(today.minusDays(29)))
                .toList();
        long remembered = recent.stream().filter(l -> l.getGrade() != Grade.AGAIN).count();
        double retention = recent.isEmpty() ? 0 : Math.round(1000.0 * remembered / recent.size()) / 1000.0;

        return new Stats(perDay.getOrDefault(today, 0L), streak(perDay.keySet(), today), retention, last30);
    }

    /** Consecutive study days ending today — or yesterday, so the streak survives until you study today. */
    static int streak(Set<LocalDate> studied, LocalDate today) {
        LocalDate d = studied.contains(today) ? today : today.minusDays(1);
        int n = 0;
        while (studied.contains(d)) {
            n++;
            d = d.minusDays(1);
        }
        return n;
    }
}
