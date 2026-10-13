package org.staymatch.auth.exception;

/**
 * Thrown when a refresh token has expired.
 */
public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}

