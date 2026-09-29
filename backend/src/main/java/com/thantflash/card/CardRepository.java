package com.thantflash.card;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("select c from Card c join fetch c.deck d where c.id = :id and d.owner.id = :ownerId")
    Optional<Card> findOwned(Long id, Long ownerId);

    @Query(value = """
            select c from Card c
            where c.deck.id = :deckId
              and (lower(c.front) like concat('%', :q, '%') or lower(c.back) like concat('%', :q, '%'))
            """)
    Page<Card> search(Long deckId, String q, Pageable pageable);

    @Query("""
            select c from Card c join fetch c.deck d
            where d.owner.id = :ownerId and c.dueAt <= :now
              and (:deckId is null or d.id = :deckId)
            order by c.dueAt
            """)
    List<Card> findDue(Long ownerId, Long deckId, Instant now, Pageable limit);

    boolean existsByDeckIdAndFrontAndBack(Long deckId, String front, String back);
}
