package com.quarkus.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.quarkus.dto.request.LoginRequest;
import com.quarkus.dto.response.TokenResponse;
import com.quarkus.entity.User;
import com.quarkus.entity.UserRole;
import com.quarkus.repository.UserRepository;
import com.quarkus.security.TokenService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class AuthServiceTest {

    @Inject
    AuthService authService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    TokenService tokenService;

    private User testUser;
    private String hashedPassword;

    @BeforeEach
    void setUp() {
        hashedPassword = BCrypt.withDefaults().hashToString(12, "password123".toCharArray());
        testUser = new User("testuser", hashedPassword, UserRole.USER);
        testUser.setId(1L);
    }

    @Test
    void shouldLoginSuccessfullyWithValidCredentials() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tokenService.generateToken(any(User.class))).thenReturn("generated-token");
        when(tokenService.getTokenLifespanInSeconds()).thenReturn(300L);

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("generated-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(300L, response.expiresIn());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(NotAuthorizedException.class, () -> authService.login(request));
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(NotAuthorizedException.class, () -> authService.login(request));
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(tokenService.generateToken(any(User.class))).thenReturn("refreshed-token");
        when(tokenService.getTokenLifespanInSeconds()).thenReturn(300L);

        TokenResponse response = authService.refresh("testuser");

        assertNotNull(response);
        assertEquals("refreshed-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(300L, response.expiresIn());
    }

    @Test
    void shouldThrowExceptionWhenRefreshingNonExistentUser() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(NotAuthorizedException.class, () -> authService.refresh("nonexistent"));
    }
}
