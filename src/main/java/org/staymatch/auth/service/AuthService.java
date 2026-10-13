package org.staymatch.auth.service;

import org.staymatch.auth.dto.AuthResponse;
import org.staymatch.auth.dto.LoginRequest;
import org.staymatch.auth.dto.RefreshTokenRequest;
import org.staymatch.auth.dto.RefreshTokenResponse;
import org.staymatch.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    /**
     * Validates the refresh token and issues a new short-lived access token.
     */
    RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request);
}

