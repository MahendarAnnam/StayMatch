package org.staymatch.auth.dto;

import java.time.Instant;

/**
 * Response DTO for user operations (registration, profile lookup).
 * Deliberately excludes password and token — clean separation.
 */
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private Instant createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String fullName, String email, String phoneNumber, String role, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

