package com.thantflash.auth;

import com.thantflash.auth.AuthDtos.LoginRequest;
import com.thantflash.auth.AuthDtos.MeResponse;
import com.thantflash.auth.AuthDtos.RegisterRequest;
import com.thantflash.auth.AuthDtos.TokenResponse;
import com.thantflash.common.NotFoundException;
import com.thantflash.user.CurrentUser;
import com.thantflash.user.User;
import com.thantflash.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository users;

    public AuthController(AuthService authService, UserRepository users) {
        this.authService = authService;
        this.users = users;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        long id = CurrentUser.id(jwt);
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("User", id));
        return new MeResponse(u.getId(), u.getEmail(), u.getDisplayName(), u.getTimeZone());
    }
}
