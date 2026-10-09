package com.thantzin.thantflash.deck;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class DeckDtos {

    private DeckDtos() {
    }

    public record DeckRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record DeckResponse(Long id, String name, long cardCount, long dueCount) {
    }
}
