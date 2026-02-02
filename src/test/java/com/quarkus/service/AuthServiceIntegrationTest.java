package com.quarkus.service;

import com.quarkus.dto.request.LoginRequest;
import com.quarkus.dto.response.TokenResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthServiceIntegrationTest {

    @Inject
    AuthService authService;

    @Test
    @Transactional
    void shouldLoginWithValidAdminCredentials() {
        LoginRequest request = new LoginRequest("admin", "admin123");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(300L, response.expiresIn());
    }

    @Test
    @Transactional
    void shouldLoginWithValidUserCredentials() {
        LoginRequest request = new LoginRequest("user", "user123");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(300L, response.expiresIn());
    }
}
