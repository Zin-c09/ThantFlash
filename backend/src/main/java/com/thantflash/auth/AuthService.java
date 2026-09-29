package com.thantflash.auth;

import com.thantflash.auth.AuthDtos.LoginRequest;
import com.thantflash.auth.AuthDtos.RegisterRequest;
import com.thantflash.auth.AuthDtos.TokenResponse;
import com.thantflash.common.ConflictException;
import com.thantflash.config.JwtProperties;
import com.thantflash.user.User;
import com.thantflash.user.UserRepository;
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

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProps;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder, JwtProperties jwtProps) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtProps = jwtProps;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("Email already registered");
        }
        User user = users.save(new User(req.email().toLowerCase(),
                passwordEncoder.encode(req.password()), req.displayName()));
        return issueToken(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("bad credentials"));
        return issueToken(user);
    }

    private TokenResponse issueToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProps.ttl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProps.issuer())
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", expiresAt);
    }
}
