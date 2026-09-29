package com.thantflash.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** HMAC secret (min 32 bytes for HS256) and access-token lifetime. */
@Validated
@ConfigurationProperties("thantflash.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        Duration ttl,
        String issuer) {

    public JwtProperties {
        if (ttl == null) ttl = Duration.ofHours(12);
        if (issuer == null) issuer = "thantflash";
    }
}
