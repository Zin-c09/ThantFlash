package com.thantzin.thantflash.review;

import java.time.ZoneId;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thantzin.thantflash.auth.CurrentUser;
import com.thantzin.thantflash.card.CardDtos.CardResponse;
import com.thantzin.thantflash.review.ReviewDtos.ReviewRequest;
import com.thantzin.thantflash.review.ReviewDtos.StatsResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Study")
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** Cards due now. Omit deckId to study all decks. */
    @GetMapping("/api/study/due")
    public List<CardResponse> due(@AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) Long deckId,
            @RequestParam(defaultValue = "50") int limit) {
        return reviewService.due(CurrentUser.id(jwt), deckId, limit);
    }

    @PostMapping("/api/cards/{cardId}/review")
    public CardResponse review(@AuthenticationPrincipal Jwt jwt, @PathVariable Long cardId,
            @Valid @RequestBody ReviewRequest req) {
        return reviewService.review(CurrentUser.id(jwt), cardId, req.grade());
    }

    /** zone decides where "today" starts, e.g. Asia/Tokyo or Asia/Yangon. */
    @GetMapping("/api/stats")
    public StatsResponse stats(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "Asia/Tokyo") ZoneId zone) {
        return reviewService.stats(CurrentUser.id(jwt), zone);
    }
}
