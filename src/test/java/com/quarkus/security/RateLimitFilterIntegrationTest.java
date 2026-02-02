package com.quarkus.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.jwt.build.Jwt;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@TestProfile(RateLimitFilterIntegrationTest.RateLimitEnabledProfile.class)
class RateLimitFilterIntegrationTest {

    public static class RateLimitEnabledProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("app.rate-limit.enabled", "true");
        }
    }

    private String generateTokenForUser(String username, String role) {
        return Jwt.issuer("quarkus-api")
                .upn(username)
                .groups(Set.of(role))
                .expiresIn(Duration.ofMinutes(5))
                .sign();
    }

    @Test
    void shouldAllowRequestsUnderLimit() {
        // Given a valid token with unique username
        String token = generateTokenForUser("testuser_" + UUID.randomUUID(), "USER");

        // When making 10 requests (the limit)
        for (int i = 0; i < 10; i++) {
            given()
                .auth().oauth2(token)
                .when()
                .get("/api/v1/artists")
                .then()
                .statusCode(200)
                .header("X-RateLimit-Limit", equalTo("10"))
                .header("X-RateLimit-Remaining", notNullValue());
        }
    }

    @Test
    void shouldBlockRequestsWhenLimitExceeded() {
        // Given a valid token with unique username
        String token = generateTokenForUser("testuser_" + UUID.randomUUID(), "USER");

        // When making 10 requests (the limit)
        for (int i = 0; i < 10; i++) {
            given()
                .auth().oauth2(token)
                .when()
                .get("/api/v1/artists")
                .then()
                .statusCode(200);
        }

        // Then the 11th request should be blocked
        given()
            .auth().oauth2(token)
            .when()
            .get("/api/v1/artists")
            .then()
            .statusCode(429)
            .header("X-RateLimit-Limit", equalTo("10"))
            .header("X-RateLimit-Remaining", equalTo("0"))
            .header("X-RateLimit-Reset", notNullValue())
            .body("status", equalTo(429))
            .body("message", equalTo("Too Many Requests"))
            .body("path", notNullValue())
            .body("timestamp", notNullValue());
    }

    @Test
    void shouldIsolateLimitsByUser() {
        // Given two different users with unique usernames
        String userToken = generateTokenForUser("testuser_" + UUID.randomUUID(), "USER");
        String adminToken = generateTokenForUser("testadmin_" + UUID.randomUUID(), "ADMIN");

        // When user exhausts their limit
        for (int i = 0; i < 10; i++) {
            given()
                .auth().oauth2(userToken)
                .when()
                .get("/api/v1/artists")
                .then()
                .statusCode(200);
        }

        // Then admin should still be able to make requests
        given()
            .auth().oauth2(adminToken)
            .when()
            .get("/api/v1/artists")
            .then()
            .statusCode(200)
            .header("X-RateLimit-Remaining", notNullValue());
    }


    @Test
    void shouldIncludeRateLimitHeadersInSuccessfulResponses() {
        // Given a valid token with unique username
        String token = generateTokenForUser("testuser_" + UUID.randomUUID(), "USER");

        // When making a request
        given()
            .auth().oauth2(token)
            .when()
            .get("/api/v1/artists")
            .then()
            .statusCode(200)
            .header("X-RateLimit-Limit", equalTo("10"))
            .header("X-RateLimit-Remaining", notNullValue());
    }

    @Test
    void shouldDecrementRemainingTokensWithEachRequest() {
        // Given a valid token with unique username
        String token = generateTokenForUser("testuser_" + UUID.randomUUID(), "USER");

        // When making sequential requests
        for (int i = 9; i >= 0; i--) {
            given()
                .auth().oauth2(token)
                .when()
                .get("/api/v1/artists")
                .then()
                .statusCode(200)
                .header("X-RateLimit-Limit", equalTo("10"))
                .header("X-RateLimit-Remaining", notNullValue());
        }
    }
}
