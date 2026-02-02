package com.quarkus.security;

import com.quarkus.entity.User;
import com.quarkus.entity.UserRole;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TokenServiceTest {

    @Inject
    TokenService tokenService;

    @Inject
    JWTParser jwtParser;

    @Test
    void shouldGenerateValidTokenForAdminUser() throws ParseException {
        User user = new User("admin", "hashedPassword", UserRole.ADMIN);

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        JsonWebToken jwt = jwtParser.parse(token);
        assertEquals("admin", jwt.getName());
        assertEquals("quarkus-api", jwt.getIssuer());
        assertTrue(jwt.getGroups().contains("ADMIN"));
    }

    @Test
    void shouldGenerateValidTokenForRegularUser() throws ParseException {
        User user = new User("user", "hashedPassword", UserRole.USER);

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        JsonWebToken jwt = jwtParser.parse(token);
        assertEquals("user", jwt.getName());
        assertEquals("quarkus-api", jwt.getIssuer());
        assertTrue(jwt.getGroups().contains("USER"));
    }

    @Test
    void shouldReturnCorrectTokenLifespan() {
        long lifespan = tokenService.getTokenLifespanInSeconds();
        assertEquals(300, lifespan);
    }

    @Test
    void shouldGenerateTokenWithExpirationTime() throws ParseException {
        User user = new User("testuser", "hashedPassword", UserRole.USER);

        String token = tokenService.generateToken(user);
        JsonWebToken jwt = jwtParser.parse(token);

        assertNotNull(jwt.getExpirationTime());
        assertTrue(jwt.getExpirationTime() > System.currentTimeMillis() / 1000);
    }
}
