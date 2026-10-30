package com.societyhub.api.auth;

import com.societyhub.api.auth.dto.AuthResponse;
import com.societyhub.api.auth.dto.LoginRequest;
import com.societyhub.api.auth.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int MAX_FAILED_LOGINS = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_REGISTRATIONS_PER_IP = 10;
    private static final Duration REGISTER_WINDOW = Duration.ofHours(1);

    private final AuthService authService;
    private final AttemptLimiter attemptLimiter;
    private final EmailDomainPolicy emailDomainPolicy;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        String key = "register:" + http.getRemoteAddr();
        attemptLimiter.assertNotBlocked(key, MAX_REGISTRATIONS_PER_IP, REGISTER_WINDOW);
        attemptLimiter.recordAttempt(key);

        emailDomainPolicy.check(request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Limited per account, so guessing one person's password is slowed down from anywhere.
        String key = "login:" + request.getEmail().trim().toLowerCase();
        attemptLimiter.assertNotBlocked(key, MAX_FAILED_LOGINS, LOGIN_WINDOW);

        try {
            AuthResponse response = authService.login(request);
            attemptLimiter.clear(key);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            attemptLimiter.recordAttempt(key);
            throw e;
        }
    }
}