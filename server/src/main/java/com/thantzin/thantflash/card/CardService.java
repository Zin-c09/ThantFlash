package com.thantzin.thantflash.card;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thantzin.thantflash.card.CardDtos.CardRequest;
import com.thantzin.thantflash.card.CardDtos.CardResponse;
import com.thantzin.thantflash.card.CardDtos.CardUpdateRequest;
import com.thantzin.thantflash.card.CardDtos.ImportResult;
import com.thantzin.thantflash.common.NotFoundException;
import com.thantzin.thantflash.deck.Deck;
import com.thantzin.thantflash.deck.DeckRepository;
import com.thantzin.thantflash.deck.DeckService;

@Service
@Transactional
public class CardService {

    private final CardRepository cards;
    private final DeckRepository decks;
    private final DeckService deckService;
    private final Clock clock;

    public CardService(CardRepository cards, DeckRepository decks, DeckService deckService, Clock clock) {
        this.cards = cards;
        this.decks = decks;
        this.deckService = deckService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CardResponse> list(Long userId, Long deckId, String q) {
        Deck deck = deckService.get(userId, deckId);
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return cards.search(deck.getId(), query).stream().map(CardResponse::from).toList();
    }

    public CardResponse create(Long userId, Long deckId, CardRequest req) {
        Deck deck = deckService.get(userId, deckId);
        Card card = cards.save(new Card(deck, req.front().trim(), req.back().trim(), clock.instant()));
        return CardResponse.from(card);
    }

    public CardResponse update(Long userId, Long cardId, CardUpdateRequest req) {
        Card card = getOwned(userId, cardId);
        Deck target = deckService.get(userId, req.deckId());
        card.edit(target, req.front().trim(), req.back().trim());
        return CardResponse.from(card);
    }

    public void delete(Long userId, Long cardId) {
        cards.delete(getOwned(userId, cardId));
    }

    /** Imports TSV rows, creating decks by name when needed and skipping duplicate fronts. */
    public ImportResult importTsv(Long userId, String tsv, String fallbackDeck) {
        Map<String, Deck> byName = new HashMap<>();
        int imported = 0;
        int skipped = 0;
        String fallback = fallbackDeck == null || fallbackDeck.isBlank() ? "Imported" : fallbackDeck.trim();
        for (TsvParser.Row row : TsvParser.parse(tsv, fallback)) {
            String deckName = row.deck().length() > 100 ? row.deck().substring(0, 100) : row.deck();
            Deck deck = byName.computeIfAbsent(deckName, name -> decks.findByUserIdAndName(userId, name)
                    .orElseGet(() -> decks.save(new Deck(userId, name, clock.instant()))));
            if (row.front().length() > 2000 || row.back().length() > 2000
                    || cards.existsByDeckAndFront(deck, row.front())) {
                skipped++;
                continue;
            }
            cards.save(new Card(deck, row.front(), row.back(), clock.instant()));
            imported++;
        }
        return new ImportResult(imported, skipped);
    }

    @Transactional(readOnly = true)
    public Card getOwned(Long userId, Long cardId) {
        return cards.findOwned(cardId, userId).orElseThrow(() -> new NotFoundException("Card", cardId));
    }
}
