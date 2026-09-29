package com.thantzin.thantflash.auth;

import org.springframework.security.oauth2.jwt.Jwt;

/** The JWT "sub" claim holds the user id. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
