package org.staymatch.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.staymatch.auth.dto.AuthResponse;
import org.staymatch.auth.dto.LoginRequest;
import org.staymatch.auth.dto.RefreshTokenRequest;
import org.staymatch.auth.dto.RefreshTokenResponse;
import org.staymatch.auth.dto.RegisterRequest;
import org.staymatch.auth.dto.UserResponse;
import org.staymatch.auth.model.AppUser;
import org.staymatch.auth.model.RefreshToken;
import org.staymatch.auth.repository.AppUserRepository;
import org.staymatch.auth.security.JwtUtil;
import org.staymatch.auth.service.AuthService;
import org.staymatch.auth.service.RefreshTokenService;
import org.staymatch.auth.service.UserService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserService userService;
    private final AppUserRepository appUserRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserService userService,
                           AppUserRepository appUserRepository,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.appUserRepository = appUserRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Delegate user creation to UserService (SRP)
        UserResponse userResponse = userService.registerUser(request);

        // Fetch the persisted entity for refresh token association
        AppUser user = appUserRepository.findById(userResponse.getId())
            .orElseThrow(() -> new IllegalStateException("User not found after registration"));

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User registered and tokens generated: userId={}", user.getId());
        return new AuthResponse(accessToken, refreshToken.getToken(),
            user.getId(), user.getEmail(), user.getRole().name(),
            user.getFullName(), user.getPhoneNumber());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        AppUser user = appUserRepository.findByEmailIgnoreCase(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in: id={}, email={}", user.getId(), user.getEmail());
        return new AuthResponse(accessToken, refreshToken.getToken(),
            user.getId(), user.getEmail(), user.getRole().name(),
            user.getFullName(), user.getPhoneNumber());
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        AppUser user = refreshToken.getUser();
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        log.info("Access token refreshed for userId={}", user.getId());
        return new RefreshTokenResponse(accessToken);
    }
}
