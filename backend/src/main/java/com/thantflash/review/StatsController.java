package com.thantflash.review;

import com.thantflash.user.CurrentUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/api/stats")
    public StatsService.Stats stats(@AuthenticationPrincipal Jwt jwt) {
        return statsService.stats(CurrentUser.id(jwt));
    }
}
