package com.thantzin.thantflash.deck;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    Optional<Deck> findByIdAndUserId(Long id, Long userId);

    Optional<Deck> findByUserIdAndName(Long userId, String name);

    boolean existsByUserIdAndName(Long userId, String name);

    /** Every deck of the user with its card count and due-card count, in one query. */
    @Query("""
            select new com.thantzin.thantflash.deck.DeckDtos$DeckResponse(
                d.id, d.name, count(c), coalesce(sum(case when c.dueAt <= :now then 1 else 0 end), 0))
            from Deck d left join Card c on c.deck = d
            where d.userId = :userId
            group by d.id, d.name
            order by d.name
            """)
    List<DeckDtos.DeckResponse> findSummaries(Long userId, Instant now);
}
