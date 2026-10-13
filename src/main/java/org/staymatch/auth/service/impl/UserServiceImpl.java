package org.staymatch.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.staymatch.auth.dto.RegisterRequest;
import org.staymatch.auth.dto.UserResponse;
import org.staymatch.auth.mapper.UserMapper;
import org.staymatch.auth.model.AppUser;
import org.staymatch.auth.model.Role;
import org.staymatch.auth.repository.AppUserRepository;
import org.staymatch.auth.service.UserService;
import org.staymatch.common.exception.ResourceNotFoundException;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (appUserRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        Role role = parseRole(request.getRole());

        AppUser user = new AppUser();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(role);

        AppUser saved = appUserRepository.save(user);
        log.info("User registered: id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id={}", id);
        AppUser user = appUserRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("User not found with id={}", id);
                return new ResourceNotFoundException("User not found with id: " + id);
            });
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email={}", email);
        AppUser user = appUserRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> {
                log.warn("User not found with email={}", email);
                return new ResourceNotFoundException("User not found with email: " + email);
            });
        return userMapper.toResponse(user);
    }

    // --- Private helpers ---

    private Role parseRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid role: " + roleStr + ". Must be one of: PG_OWNER, TENANT, ADMIN");
        }
    }
}

