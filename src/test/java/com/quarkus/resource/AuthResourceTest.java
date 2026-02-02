package com.quarkus.resource;

import com.quarkus.dto.request.LoginRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class AuthResourceTest {

    @Test
    void shouldLoginSuccessfullyWithAdminCredentials() {
        LoginRequest request = new LoginRequest("admin", "admin123");

        given()
            .contentType(ContentType.JSON)
            .body(request)
            .log().all()
        .when()
            .post("/api/v1/auth/login")
        .then()
            .log().all()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("tokenType", equalTo("Bearer"))
            .body("expiresIn", equalTo(300));
    }

    @Test
    void shouldLoginSuccessfullyWithUserCredentials() {
        LoginRequest request = new LoginRequest("user", "user123");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("tokenType", equalTo("Bearer"))
            .body("expiresIn", equalTo(300));
    }

    @Test
    void shouldReturn401WithInvalidUsername() {
        LoginRequest request = new LoginRequest("invalid", "password");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldReturn401WithInvalidPassword() {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldReturn400WithMissingUsername() {
        String invalidRequest = "{\"password\": \"password123\"}";

        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldReturn400WithMissingPassword() {
        String invalidRequest = "{\"username\": \"admin\"}";

        given()
            .contentType(ContentType.JSON)
            .body(invalidRequest)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        LoginRequest loginRequest = new LoginRequest("user", "user123");

        String token = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .path("accessToken");

        given()
            .auth().oauth2(token)
        .when()
            .post("/api/v1/auth/refresh")
        .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("tokenType", equalTo("Bearer"))
            .body("expiresIn", equalTo(300));
    }

    @Test
    void shouldReturn401WhenRefreshingWithoutToken() {
        given()
        .when()
            .post("/api/v1/auth/refresh")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldReturn401WhenRefreshingWithInvalidToken() {
        given()
            .auth().oauth2("invalid-token")
        .when()
            .post("/api/v1/auth/refresh")
        .then()
            .statusCode(401);
    }
}
