package org.staymatch.auth.service.impl;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.staymatch.auth.exception.RefreshTokenExpiredException;
import org.staymatch.auth.model.AppUser;
import org.staymatch.auth.model.RefreshToken;
import org.staymatch.auth.repository.RefreshTokenRepository;
import org.staymatch.auth.service.RefreshTokenService;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationMs;

    public RefreshTokenServiceImpl(
        RefreshTokenRepository refreshTokenRepository,
        @Value("${app.jwt.refresh-expiration-ms}") long refreshTokenExpirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(AppUser user) {
        // Remove old tokens for this user to enforce single-session
        deleteOldTokens(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpirationMs));

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token created for userId={}", user.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> {
                log.warn("Refresh token not found: {}", token);
                return new IllegalArgumentException("Refresh token not found");
            });

        if (refreshToken.isExpired()) {
            log.warn("Refresh token expired for userId={}", refreshToken.getUser().getId());
            throw new RefreshTokenExpiredException("Refresh token has expired. Please login again.");
        }

        log.debug("Refresh token validated for userId={}", refreshToken.getUser().getId());
        return refreshToken;
    }

    @Override
    @Transactional
    public void deleteOldTokens(AppUser user) {
        refreshTokenRepository.deleteByUser(user);
        log.debug("Deleted old refresh tokens for userId={}", user.getId());
    }
}

