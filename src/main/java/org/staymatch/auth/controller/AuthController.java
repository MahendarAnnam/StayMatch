package org.staymatch.auth.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.staymatch.auth.dto.AuthResponse;
import org.staymatch.auth.dto.LoginRequest;
import org.staymatch.auth.dto.RefreshTokenRequest;
import org.staymatch.auth.dto.RefreshTokenResponse;
import org.staymatch.auth.dto.RegisterRequest;
import org.staymatch.auth.dto.UserResponse;
import org.staymatch.auth.service.AuthService;
import org.staymatch.auth.service.UserService;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Register a new user and return access + refresh tokens.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticate an existing user and return access + refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Refresh the access token using a valid refresh token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshAccessToken(request));
    }

    /**
     * Get user profile by ID. Requires authentication.
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipGuard.getAuthenticatedUserId() == #id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
