package org.staymatch.auth.dto;

/**
 * Response DTO for token refresh endpoint.
 * Returns only the new access token — the refresh token remains unchanged.
 */
public class RefreshTokenResponse {

    private String accessToken;

    public RefreshTokenResponse() {
    }

    public RefreshTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}

