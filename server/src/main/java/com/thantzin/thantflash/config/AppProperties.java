package com.thantzin.thantflash.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Values from the {@code thantflash.*} section of application.yml. */
@ConfigurationProperties(prefix = "thantflash")
public record AppProperties(Jwt jwt, Cors cors) {

    public record Jwt(String secret, Duration expiresIn) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
