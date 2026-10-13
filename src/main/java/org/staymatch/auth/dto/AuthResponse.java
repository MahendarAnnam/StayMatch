package org.staymatch.auth.dto;

public class AuthResponse {

    private String token;
    private String refreshToken;
    private Long userId;
    private String email;
    private String role;
    private String fullName;
    private String phoneNumber;

    public AuthResponse(String token, String refreshToken, Long userId, String email, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public AuthResponse(String token, String refreshToken, Long userId, String email, String role, String fullName, String phoneNumber) {
        this(token, refreshToken, userId, email, role);
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}

