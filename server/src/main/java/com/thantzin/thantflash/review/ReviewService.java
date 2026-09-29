package com.thantzin.thantflash.review;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thantzin.thantflash.card.Card;
import com.thantzin.thantflash.card.CardDtos.CardResponse;
import com.thantzin.thantflash.card.CardRepository;
import com.thantzin.thantflash.card.CardService;
import com.thantzin.thantflash.deck.DeckService;
import com.thantzin.thantflash.review.ReviewDtos.DailyCount;
import com.thantzin.thantflash.review.ReviewDtos.StatsResponse;

@Service
@Transactional
public class ReviewService {

    /** Streak is counted over at most this many days back. */
    private static final int STREAK_WINDOW_DAYS = 365;

    private final CardRepository cards;
    private final CardService cardService;
    private final DeckService deckService;
    private final ReviewLogRepository logs;
    private final Sm2Scheduler scheduler;
    private final Clock clock;

    public ReviewService(CardRepository cards, CardService cardService, DeckService deckService,
            ReviewLogRepository logs,
            Sm2Scheduler scheduler, Clock clock) {
        this.cards = cards;
        this.cardService = cardService;
        this.deckService = deckService;
        this.logs = logs;
        this.scheduler = scheduler;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CardResponse> due(Long userId, Long deckId, int limit) {
        if (deckId != null) {
            deckService.get(userId, deckId); // 404 if the deck isn't the user's
        }
        int size = Math.min(Math.max(limit, 1), 200);
        return cards.findDue(userId, deckId, clock.instant(), PageRequest.of(0, size)).stream()
                .map(CardResponse::from).toList();
    }

    public CardResponse review(Long userId, Long cardId, Grade grade) {
        Card card = cardService.getOwned(userId, cardId);
        Instant now = clock.instant();
        card.applyReview(scheduler.next(card.schedule(), grade, now));
        logs.save(new ReviewLog(card.getId(), userId, grade, now));
        return CardResponse.from(card);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats(Long userId, ZoneId zone) {
        Instant now = clock.instant();
        LocalDate today = LocalDate.ofInstant(now, zone);
        Instant from = today.minusDays(STREAK_WINDOW_DAYS).atStartOfDay(zone).toInstant();
        List<ReviewLog> recent = logs.findByUserIdAndReviewedAtGreaterThanEqual(userId, from);

        Map<LocalDate, Long> perDay = recent.stream().collect(Collectors.groupingBy(
                l -> LocalDate.ofInstant(l.getReviewedAt(), zone), Collectors.counting()));

        List<DailyCount> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            last7.add(new DailyCount(d, perDay.getOrDefault(d, 0L)));
        }

        Instant weekStart = today.minusDays(6).atStartOfDay(zone).toInstant();
        Map<Boolean, Long> correct = recent.stream()
                .filter(l -> !l.getReviewedAt().isBefore(weekStart))
                .collect(Collectors.partitioningBy(l -> l.getGrade() != Grade.AGAIN, Collectors.counting()));
        long weekTotal = correct.get(true) + correct.get(false);
        double accuracy = weekTotal == 0 ? 0 : Math.round(correct.get(true) * 1000.0 / weekTotal) / 1000.0;

        return new StatsResponse(cards.countByUser(userId), cards.countDue(userId, now),
                perDay.getOrDefault(today, 0L), accuracy, streak(perDay::containsKey, today), last7);
    }

    /** Today not studied yet doesn't break the streak — it counts back from yesterday then. */
    static int streak(Predicate<LocalDate> studied, LocalDate today) {
        LocalDate d = studied.test(today) ? today : today.minusDays(1);
        int n = 0;
        while (n < STREAK_WINDOW_DAYS && studied.test(d)) {
            n++;
            d = d.minusDays(1);
        }
        return n;
    }
}
