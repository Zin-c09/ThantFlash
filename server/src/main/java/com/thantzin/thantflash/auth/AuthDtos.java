package com.thantzin.thantflash.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50)
            @Pattern(regexp = "[A-Za-z0-9_.-]+", message = "letters, numbers, _ . - only")
            String username,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record TokenResponse(String token, long expiresInSeconds, String username) {
    }
}
