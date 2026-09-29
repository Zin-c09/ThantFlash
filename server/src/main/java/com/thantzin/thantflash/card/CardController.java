package com.thantzin.thantflash.card;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.thantzin.thantflash.auth.CurrentUser;
import com.thantzin.thantflash.card.CardDtos.CardRequest;
import com.thantzin.thantflash.card.CardDtos.CardResponse;
import com.thantzin.thantflash.card.CardDtos.CardUpdateRequest;
import com.thantzin.thantflash.card.CardDtos.ImportResult;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Cards")
@RestController
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/api/decks/{deckId}/cards")
    public List<CardResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable Long deckId,
            @RequestParam(required = false) String q) {
        return cardService.list(CurrentUser.id(jwt), deckId, q);
    }

    @PostMapping("/api/decks/{deckId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable Long deckId,
            @Valid @RequestBody CardRequest req) {
        return cardService.create(CurrentUser.id(jwt), deckId, req);
    }

    @PutMapping("/api/cards/{cardId}")
    public CardResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long cardId,
            @Valid @RequestBody CardUpdateRequest req) {
        return cardService.update(CurrentUser.id(jwt), cardId, req);
    }

    @DeleteMapping("/api/cards/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long cardId) {
        cardService.delete(CurrentUser.id(jwt), cardId);
    }

    /** Body is the raw .tsv file content (Content-Type: text/plain). */
    @PostMapping(path = "/api/import/tsv", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ImportResult importTsv(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "Imported") String deck, @RequestBody String tsv) {
        return cardService.importTsv(CurrentUser.id(jwt), tsv, deck);
    }
}
