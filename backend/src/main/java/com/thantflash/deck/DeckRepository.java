package com.thantflash.deck;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    Optional<Deck> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Deck> findByOwnerIdAndName(Long ownerId, String name);

    boolean existsByOwnerIdAndName(Long ownerId, String name);

    /** One query for the deck list with card totals and due counts — avoids N+1. */
    @Query("""
            select new com.thantflash.deck.DeckDtos$DeckSummary(
                d.id, d.name, count(c.id),
                coalesce(sum(case when c.dueAt <= :now then 1 else 0 end), 0))
            from Deck d left join Card c on c.deck = d
            where d.owner.id = :ownerId
            group by d.id, d.name
            order by d.name
            """)
    List<DeckDtos.DeckSummary> summarize(Long ownerId, java.time.Instant now);
}
