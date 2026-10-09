package com.thantzin.thantflash.deck;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thantzin.thantflash.auth.CurrentUser;
import com.thantzin.thantflash.deck.DeckDtos.DeckRequest;
import com.thantzin.thantflash.deck.DeckDtos.DeckResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Decks")
@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public List<DeckResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return deckService.list(CurrentUser.id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeckResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeckRequest req) {
        return deckService.create(CurrentUser.id(jwt), req);
    }

    @PutMapping("/{deckId}")
    public DeckResponse rename(@AuthenticationPrincipal Jwt jwt, @PathVariable Long deckId,
            @Valid @RequestBody DeckRequest req) {
        return deckService.rename(CurrentUser.id(jwt), deckId, req);
    }

    @DeleteMapping("/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long deckId) {
        deckService.delete(CurrentUser.id(jwt), deckId);
    }
}
