package com.thantflash.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;

public final class CardDtos {

    private CardDtos() {
    }

    public record CardRequest(
            @NotBlank @Size(max = 2000) String front,
            @NotBlank @Size(max = 2000) String back,
            Long deckId) {
    }

    public record ReviewRequest(@NotNull Grade grade) {
    }

    public record CardResponse(long id, long deckId, String deckName, String front, String back,
                               double intervalDays, double ease, int reps, Instant dueAt) {

        static CardResponse of(Card c) {
            return new CardResponse(c.getId(), c.getDeck().getId(), c.getDeck().getName(),
                    c.getFront(), c.getBack(), c.getIntervalDays(), c.getEase(), c.getReps(), c.getDueAt());
        }
    }

    public record ImportResult(int imported, int skippedDuplicates, int invalidLines) {
    }

    /** Stable page shape (Spring's PageImpl JSON is not a guaranteed contract). */
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

        static <S, T> PageResponse<T> of(Page<S> p, java.util.function.Function<S, T> map) {
            return new PageResponse<>(p.getContent().stream().map(map).toList(),
                    p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        }
    }
}
