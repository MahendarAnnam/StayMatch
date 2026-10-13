package org.staymatch.auth.service;

import org.staymatch.auth.model.AppUser;
import org.staymatch.auth.model.RefreshToken;

/**
 * Manages refresh token lifecycle: creation, validation, and cleanup.
 */
public interface RefreshTokenService {

    /**
     * Creates a new refresh token for the given user.
     * Deletes any existing tokens for the user before creating.
     */
    RefreshToken createRefreshToken(AppUser user);

    /**
     * Validates the refresh token string and returns the entity.
     * Throws exception if token is not found or expired.
     */
    RefreshToken validateRefreshToken(String token);

    /**
     * Deletes all refresh tokens for the given user.
     */
    void deleteOldTokens(AppUser user);
}

