package com.thantzin.thantflash.card;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("select c from Card c join fetch c.deck d where c.id = :id and d.userId = :userId")
    Optional<Card> findOwned(Long id, Long userId);

    @Query("""
            select c from Card c join fetch c.deck d
            where d.id = :deckId
              and (:q is null or lower(c.front) like lower(concat('%', :q, '%'))
                              or lower(c.back)  like lower(concat('%', :q, '%')))
            order by c.createdAt, c.id
            """)
    List<Card> search(Long deckId, String q);

    /** Cards to study now, oldest due first. deckId = null means all decks. */
    @Query("""
            select c from Card c join fetch c.deck d
            where d.userId = :userId and (:deckId is null or d.id = :deckId) and c.dueAt <= :now
            order by c.dueAt, c.id
            """)
    List<Card> findDue(Long userId, Long deckId, Instant now, Pageable page);

    @Query("select count(c) from Card c where c.deck.userId = :userId")
    long countByUser(Long userId);

    @Query("select count(c) from Card c where c.deck.userId = :userId and c.dueAt <= :now")
    long countDue(Long userId, Instant now);

    boolean existsByDeckAndFront(com.thantzin.thantflash.deck.Deck deck, String front);
}
