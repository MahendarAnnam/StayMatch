package org.staymatch.auth.service;

import org.staymatch.auth.dto.RegisterRequest;
import org.staymatch.auth.dto.UserResponse;

/**
 * Handles user lifecycle operations (registration, lookup).
 * Separated from AuthService following Single Responsibility Principle.
 */
public interface UserService {

    UserResponse registerUser(RegisterRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);
}

