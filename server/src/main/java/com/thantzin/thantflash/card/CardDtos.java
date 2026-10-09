package com.thantzin.thantflash.card;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CardDtos {

    private CardDtos() {
    }

    public record CardRequest(
            @NotBlank @Size(max = 2000) String front,
            @NotBlank @Size(max = 2000) String back) {
    }

    /** For PUT: deckId lets you move a card to another deck. */
    public record CardUpdateRequest(
            @NotNull Long deckId,
            @NotBlank @Size(max = 2000) String front,
            @NotBlank @Size(max = 2000) String back) {
    }

    public record CardResponse(Long id, Long deckId, String deckName, String front, String back,
            double intervalDays, double ease, Instant dueAt, int reps) {

        public static CardResponse from(Card c) {
            return new CardResponse(c.getId(), c.getDeck().getId(), c.getDeck().getName(), c.getFront(),
                    c.getBack(), c.getIntervalDays(), c.getEase(), c.getDueAt(), c.getReps());
        }
    }

    public record ImportResult(int imported, int skipped) {
    }
}
