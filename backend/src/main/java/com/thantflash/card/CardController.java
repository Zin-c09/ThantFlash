package com.thantflash.card;

import com.thantflash.card.CardDtos.CardRequest;
import com.thantflash.card.CardDtos.CardResponse;
import com.thantflash.card.CardDtos.ImportResult;
import com.thantflash.card.CardDtos.PageResponse;
import com.thantflash.card.CardDtos.ReviewRequest;
import com.thantflash.user.CurrentUser;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/decks/{deckId}/cards")
    public PageResponse<CardResponse> list(@AuthenticationPrincipal Jwt jwt, @PathVariable long deckId,
                                           @RequestParam(required = false) String q,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), Sort.by("createdAt").descending());
        return PageResponse.of(cardService.search(CurrentUser.id(jwt), deckId, q, pageable), CardResponse::of);
    }

    @PostMapping("/decks/{deckId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse create(@AuthenticationPrincipal Jwt jwt, @PathVariable long deckId,
                               @Valid @RequestBody CardRequest req) {
        return CardResponse.of(cardService.create(CurrentUser.id(jwt), deckId, req.front(), req.back()));
    }

    @GetMapping("/cards/{id}")
    public CardResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return CardResponse.of(cardService.get(CurrentUser.id(jwt), id));
    }

    @PutMapping("/cards/{id}")
    public CardResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                               @Valid @RequestBody CardRequest req) {
        return CardResponse.of(cardService.update(CurrentUser.id(jwt), id, req.front(), req.back(), req.deckId()));
    }

    @DeleteMapping("/cards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        cardService.delete(CurrentUser.id(jwt), id);
    }

    @GetMapping("/study/due")
    public List<CardResponse> due(@AuthenticationPrincipal Jwt jwt,
                                  @RequestParam(required = false) Long deckId,
                                  @RequestParam(defaultValue = "50") int limit) {
        return cardService.dueQueue(CurrentUser.id(jwt), deckId, limit).stream().map(CardResponse::of).toList();
    }

    @PostMapping("/cards/{id}/review")
    public CardResponse review(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
                               @Valid @RequestBody ReviewRequest req) {
        return CardResponse.of(cardService.review(CurrentUser.id(jwt), id, req.grade()));
    }

    @PostMapping(value = "/import/tsv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResult importTsv(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file,
                                  @RequestParam(defaultValue = "Imported") String deck) throws IOException {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        return cardService.importTsv(CurrentUser.id(jwt), text, deck);
    }
}
