package com.thantflash.card;

import com.thantflash.card.CardDtos.ImportResult;
import com.thantflash.common.NotFoundException;
import com.thantflash.deck.Deck;
import com.thantflash.deck.DeckService;
import com.thantflash.review.ReviewLog;
import com.thantflash.review.ReviewLogRepository;
import com.thantflash.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private final CardRepository cards;
    private final DeckService deckService;
    private final ReviewLogRepository reviewLogs;
    private final UserRepository users;
    private final Clock clock;

    public CardService(CardRepository cards, DeckService deckService, ReviewLogRepository reviewLogs,
                       UserRepository users, Clock clock) {
        this.cards = cards;
        this.deckService = deckService;
        this.reviewLogs = reviewLogs;
        this.users = users;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<Card> search(long userId, long deckId, String q, Pageable pageable) {
        deckService.get(userId, deckId); // ownership check
        // Empty pattern matches everything; avoids untyped-null parameters on PostgreSQL.
        return cards.search(deckId, q == null ? "" : q.trim().toLowerCase(java.util.Locale.ROOT), pageable);
    }

    @Transactional
    public Card create(long userId, long deckId, String front, String back) {
        Deck deck = deckService.get(userId, deckId);
        return cards.save(new Card(deck, front.trim(), back.trim(), clock.instant()));
    }

    @Transactional(readOnly = true)
    public Card get(long userId, long cardId) {
        return cards.findOwned(cardId, userId).orElseThrow(() -> new NotFoundException("Card", cardId));
    }

    @Transactional
    public Card update(long userId, long cardId, String front, String back, Long deckId) {
        Card card = get(userId, cardId);
        card.setFront(front.trim());
        card.setBack(back.trim());
        if (deckId != null && !deckId.equals(card.getDeck().getId())) {
            card.setDeck(deckService.get(userId, deckId));
        }
        return card;
    }

    @Transactional
    public void delete(long userId, long cardId) {
        cards.delete(get(userId, cardId));
    }

    @Transactional(readOnly = true)
    public List<Card> dueQueue(long userId, Long deckId, int limit) {
        return cards.findDue(userId, deckId, clock.instant(), PageRequest.of(0, Math.clamp(limit, 1, 200)));
    }

    /** Applies SM-2 and appends to the review log in one transaction. */
    @Transactional
    public Card review(long userId, long cardId, Grade grade) {
        Card card = get(userId, cardId);
        Instant now = clock.instant();
        card.apply(Sm2Scheduler.next(card.schedule(), grade, now));
        reviewLogs.save(new ReviewLog(card, users.getReferenceById(userId), grade, card.getIntervalDays(), now));
        return card;
    }

    @Transactional
    public ImportResult importTsv(long userId, String text, String defaultDeck) {
        TsvParser.Result parsed = TsvParser.parse(text, defaultDeck);
        Map<String, Deck> deckCache = new HashMap<>();
        Instant now = clock.instant();
        int imported = 0;
        int dupes = 0;
        for (TsvParser.Row row : parsed.rows()) {
            Deck deck = deckCache.computeIfAbsent(row.deck(), name -> deckService.findOrCreate(userId, name));
            if (cards.existsByDeckIdAndFrontAndBack(deck.getId(), row.front(), row.back())) {
                dupes++;
                continue;
            }
            cards.save(new Card(deck, row.front(), row.back(), now));
            imported++;
        }
        return new ImportResult(imported, dupes, parsed.invalidLines());
    }
}
