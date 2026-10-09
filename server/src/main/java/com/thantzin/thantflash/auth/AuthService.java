package com.thantzin.thantflash.auth;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thantzin.thantflash.auth.AuthDtos.LoginRequest;
import com.thantzin.thantflash.auth.AuthDtos.RegisterRequest;
import com.thantzin.thantflash.auth.AuthDtos.TokenResponse;
import com.thantzin.thantflash.common.ConflictException;
import com.thantzin.thantflash.config.AppProperties;
import com.thantzin.thantflash.user.User;
import com.thantzin.thantflash.user.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final AppProperties props;
    private final Clock clock;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
            AppProperties props, Clock clock) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.props = props;
        this.clock = clock;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new ConflictException("Username already taken");
        }
        User user = users.save(new User(req.username(), passwordEncoder.encode(req.password()), clock.instant()));
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = users.findByUsername(req.username())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));
        return issueToken(user);
    }

    private TokenResponse issueToken(User user) {
        Instant now = clock.instant();
        long ttl = props.jwt().expiresIn().toSeconds();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("thantflash")
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttl))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, ttl, user.getUsername());
    }
}
