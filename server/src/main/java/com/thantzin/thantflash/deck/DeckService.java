package com.thantzin.thantflash.deck;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thantzin.thantflash.common.ConflictException;
import com.thantzin.thantflash.common.NotFoundException;
import com.thantzin.thantflash.deck.DeckDtos.DeckRequest;
import com.thantzin.thantflash.deck.DeckDtos.DeckResponse;

@Service
@Transactional
public class DeckService {

    private final DeckRepository decks;
    private final Clock clock;

    public DeckService(DeckRepository decks, Clock clock) {
        this.decks = decks;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<DeckResponse> list(Long userId) {
        return decks.findSummaries(userId, clock.instant());
    }

    public DeckResponse create(Long userId, DeckRequest req) {
        String name = req.name().trim();
        if (decks.existsByUserIdAndName(userId, name)) {
            throw new ConflictException("Deck '" + name + "' already exists");
        }
        Deck deck = decks.save(new Deck(userId, name, clock.instant()));
        return new DeckResponse(deck.getId(), deck.getName(), 0, 0);
    }

    public DeckResponse rename(Long userId, Long deckId, DeckRequest req) {
        Deck deck = get(userId, deckId);
        String name = req.name().trim();
        if (!name.equals(deck.getName()) && decks.existsByUserIdAndName(userId, name)) {
            throw new ConflictException("Deck '" + name + "' already exists");
        }
        deck.rename(name);
        return list(userId).stream().filter(d -> d.id().equals(deckId)).findFirst().orElseThrow();
    }

    public void delete(Long userId, Long deckId) {
        decks.delete(get(userId, deckId)); // cards are removed by ON DELETE CASCADE
    }

    /** Returns the deck only if it belongs to the user; otherwise 404 (we don't reveal it exists). */
    @Transactional(readOnly = true)
    public Deck get(Long userId, Long deckId) {
        return decks.findByIdAndUserId(deckId, userId).orElseThrow(() -> new NotFoundException("Deck", deckId));
    }
}
