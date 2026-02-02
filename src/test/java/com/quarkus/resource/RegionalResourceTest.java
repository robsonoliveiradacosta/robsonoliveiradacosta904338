package com.quarkus.resource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.quarkus.common.PostgresResource;
import com.quarkus.util.TestTokenHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class RegionalResourceTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", 8089);
        System.setProperty("quarkus.rest-client.regional-api.url", "http://localhost:8089");
    }

    @AfterAll
    static void teardownWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    void testListRegionals_Success() {
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .when()
            .get("/api/v1/regionals")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON);
    }

    @Test
    void testListRegionals_RequiresAuthentication() {
        given()
            .when()
            .get("/api/v1/regionals")
            .then()
            .statusCode(401);
    }

    @Test
    void testSync_Success() {
        // Mock external API response
        stubFor(get(urlEqualTo("/v1/regionais"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {"id": 100, "nome": "Regional Norte"},
                        {"id": 101, "nome": "Regional Sul"}
                    ]
                    """)));

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .when()
            .post("/api/v1/regionals/sync")
            .then()
            .statusCode(200)
            .body("inserted", greaterThanOrEqualTo(0))
            .body("updated", greaterThanOrEqualTo(0))
            .body("deactivated", greaterThanOrEqualTo(0));

        // Verify WireMock was called
        verify(getRequestedFor(urlEqualTo("/v1/regionais")));
    }

    @Test
    void testSync_RequiresAdminRole() {
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .contentType(ContentType.JSON)
            .when()
            .post("/api/v1/regionals/sync")
            .then()
            .statusCode(403);
    }

    @Test
    void testSync_RequiresAuthentication() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .post("/api/v1/regionals/sync")
            .then()
            .statusCode(401);
    }

    @Test
    void testSync_InsertNewRegionals() {
        // Mock external API with new regionals
        stubFor(get(urlEqualTo("/v1/regionais"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {"id": 200, "nome": "Regional Teste A"},
                        {"id": 201, "nome": "Regional Teste B"}
                    ]
                    """)));

        // Execute sync
        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .when()
            .post("/api/v1/regionals/sync")
            .then()
            .statusCode(200)
            .body("inserted", is(2));

        // Verify regionals are in the list
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .when()
            .get("/api/v1/regionals")
            .then()
            .statusCode(200)
            .body("findAll { it.id == 200 || it.id == 201 }.size()", is(2));
    }

    @Test
    void testSync_HandleApiUnavailable() {
        // Mock external API returning error
        stubFor(get(urlEqualTo("/v1/regionais"))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": \"Service Unavailable\"}")));

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .when()
            .post("/api/v1/regionals/sync")
            .then()
            .statusCode(500);
    }
}
