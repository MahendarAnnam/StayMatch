package org.staymatch.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.staymatch.auth.dto.RegisterRequest;
import org.staymatch.auth.dto.UserResponse;
import org.staymatch.auth.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldRegisterAndFetchUser() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("testuser-" + System.nanoTime() + "@example.com");
        request.setPassword("SecurePass123");
        request.setPhoneNumber("9999999999");
        request.setRole("TENANT");

        // Register
        UserResponse registered = userService.registerUser(request);

        assertNotNull(registered.getId());
        assertEquals(request.getFullName(), registered.getFullName());
        assertEquals(request.getEmail().toLowerCase(), registered.getEmail());
        assertEquals("TENANT", registered.getRole());
        assertNotNull(registered.getCreatedAt());

        // Fetch by ID
        UserResponse fetched = userService.getUserById(registered.getId());
        assertEquals(registered.getId(), fetched.getId());
        assertEquals(registered.getEmail(), fetched.getEmail());

        // Fetch by email
        UserResponse fetchedByEmail = userService.getUserByEmail(registered.getEmail());
        assertEquals(registered.getId(), fetchedByEmail.getId());
    }

    @Test
    void shouldRejectDuplicateEmail() {
        String email = "duplicate-" + System.nanoTime() + "@example.com";

        RegisterRequest first = new RegisterRequest();
        first.setFullName("First User");
        first.setEmail(email);
        first.setPassword("SecurePass123");
        first.setRole("PG_OWNER");
        userService.registerUser(first);

        RegisterRequest second = new RegisterRequest();
        second.setFullName("Second User");
        second.setEmail(email);
        second.setPassword("SecurePass456");
        second.setRole("TENANT");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(second));
    }

    @Test
    void shouldRejectInvalidRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Bad Role");
        request.setEmail("badrole-" + System.nanoTime() + "@example.com");
        request.setPassword("SecurePass123");
        request.setRole("INVALID_ROLE");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
    }
}

