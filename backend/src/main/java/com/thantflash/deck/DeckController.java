package com.thantflash.deck;

import com.thantflash.deck.DeckDtos.DeckRequest;
import com.thantflash.deck.DeckDtos.DeckSummary;
import com.thantflash.user.CurrentUser;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public List<DeckSummary> list(@AuthenticationPrincipal Jwt jwt) {
        return deckService.list(CurrentUser.id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeckSummary create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeckRequest req) {
        Deck d = deckService.create(CurrentUser.id(jwt), req.name());
        return new DeckSummary(d.getId(), d.getName(), 0, 0);
    }

    @PutMapping("/{id}")
    public DeckSummary rename(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                              @Valid @RequestBody DeckRequest req) {
        long userId = CurrentUser.id(jwt);
        deckService.rename(userId, id, req.name());
        return deckService.list(userId).stream().filter(s -> s.id() == id).findFirst().orElseThrow();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        deckService.delete(CurrentUser.id(jwt), id);
    }
}
