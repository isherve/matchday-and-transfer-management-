package com.ferwafa.auth;

import com.ferwafa.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private static final String TOKEN_COOKIE = "ferwafa_token";
    private static final int COOKIE_MAX_AGE = 86400;

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Admin or Team Manager login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletResponse response) {
        AuthResponse auth = authService.login(request);
        attachTokenCookie(response, auth.getToken());
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/referee-login")
    @Operation(summary = "Referee login with email and access code")
    public ResponseEntity<AuthResponse> refereeLogin(@Valid @RequestBody RefereeLoginRequest request,
                                                     HttpServletResponse response) {
        AuthResponse auth = authService.refereeLogin(request);
        attachTokenCookie(response, auth.getToken());
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/logout")
    @Operation(summary = "Clear auth cookie")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                TOKEN_COOKIE + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<MeResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    private void attachTokenCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie",
                TOKEN_COOKIE + "=" + token + "; Path=/; Max-Age=" + COOKIE_MAX_AGE
                        + "; HttpOnly; SameSite=Lax");
    }
}
