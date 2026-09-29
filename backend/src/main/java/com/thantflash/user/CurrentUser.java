package com.thantflash.user;

import org.springframework.security.oauth2.jwt.Jwt;

/** Resolves the authenticated user's id from the JWT {@code sub} claim. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static long id(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
