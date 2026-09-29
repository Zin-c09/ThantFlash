package com.thantflash.deck;

import com.thantflash.common.ConflictException;
import com.thantflash.common.NotFoundException;
import com.thantflash.deck.DeckDtos.DeckSummary;
import com.thantflash.user.UserRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeckService {

    private final DeckRepository decks;
    private final UserRepository users;
    private final Clock clock;

    public DeckService(DeckRepository decks, UserRepository users, Clock clock) {
        this.decks = decks;
        this.users = users;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<DeckSummary> list(long userId) {
        return decks.summarize(userId, clock.instant());
    }

    @Transactional
    public Deck create(long userId, String name) {
        if (decks.existsByOwnerIdAndName(userId, name)) {
            throw new ConflictException("Deck '" + name + "' already exists");
        }
        return decks.save(new Deck(users.getReferenceById(userId), name));
    }

    /** Used by imports: returns the named deck, creating it if missing. */
    @Transactional
    public Deck findOrCreate(long userId, String name) {
        return decks.findByOwnerIdAndName(userId, name)
                .orElseGet(() -> decks.save(new Deck(users.getReferenceById(userId), name)));
    }

    @Transactional(readOnly = true)
    public Deck get(long userId, long deckId) {
        return decks.findByIdAndOwnerId(deckId, userId)
                .orElseThrow(() -> new NotFoundException("Deck", deckId));
    }

    @Transactional
    public Deck rename(long userId, long deckId, String name) {
        Deck deck = get(userId, deckId);
        if (!deck.getName().equals(name) && decks.existsByOwnerIdAndName(userId, name)) {
            throw new ConflictException("Deck '" + name + "' already exists");
        }
        deck.setName(name);
        return deck;
    }

    @Transactional
    public void delete(long userId, long deckId) {
        decks.delete(get(userId, deckId));
    }
}
