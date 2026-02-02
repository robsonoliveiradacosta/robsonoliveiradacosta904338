package com.quarkus.resource;

import com.quarkus.dto.request.ArtistRequest;
import com.quarkus.entity.ArtistType;
import com.quarkus.util.TestTokenHelper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class ArtistResourceTest {

    @Test
    void testListArtists() {
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .when()
            .get("/api/v1/artists")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    void testListArtists_WithNameFilter() {
        // First create an artist
        ArtistRequest createRequest = new ArtistRequest("Queen", ArtistType.BAND);

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(201);

        // Then filter by name
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .queryParam("name", "Queen")
            .when()
            .get("/api/v1/artists")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].name", containsString("Queen"));
    }

    @Test
    void testListArtists_WithSortAscending() {
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .queryParam("sort", "name:asc")
            .when()
            .get("/api/v1/artists")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void testListArtists_WithSortDescending() {
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .queryParam("sort", "name:desc")
            .when()
            .get("/api/v1/artists")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void testGetArtist_Success() {
        // First create an artist
        ArtistRequest createRequest = new ArtistRequest("Test Artist", ArtistType.SINGER);

        Integer artistId = given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then retrieve it
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .pathParam("id", artistId)
            .when()
            .get("/api/v1/artists/{id}")
            .then()
            .statusCode(200)
            .body("id", is(artistId))
            .body("name", is("Test Artist"))
            .body("type", is("SINGER"));
    }

    @Test
    void testGetArtist_NotFound() {
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .pathParam("id", 99999)
            .when()
            .get("/api/v1/artists/{id}")
            .then()
            .statusCode(404);
    }

    @Test
    void testCreateArtist_Success() {
        ArtistRequest request = new ArtistRequest("The Beatles", ArtistType.BAND);

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(201)
            .body("name", is("The Beatles"))
            .body("type", is("BAND"))
            .body("id", notNullValue());
    }

    @Test
    void testCreateArtist_ValidationError_BlankName() {
        ArtistRequest request = new ArtistRequest("", ArtistType.BAND);

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(400);
    }

    @Test
    void testCreateArtist_ValidationError_NullType() {
        String requestJson = "{\"name\":\"Test Artist\",\"type\":null}";

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(requestJson)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(400);
    }

    @Test
    void testUpdateArtist_Success() {
        // First create an artist
        ArtistRequest createRequest = new ArtistRequest("Artist to Update", ArtistType.SINGER);

        Integer artistId = given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then update it
        ArtistRequest updateRequest = new ArtistRequest("Updated Artist", ArtistType.BAND);

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .pathParam("id", artistId)
            .body(updateRequest)
            .when()
            .put("/api/v1/artists/{id}")
            .then()
            .statusCode(200)
            .body("name", is("Updated Artist"))
            .body("type", is("BAND"));
    }

    @Test
    void testUpdateArtist_NotFound() {
        ArtistRequest request = new ArtistRequest("Non-existent", ArtistType.BAND);

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .pathParam("id", 99999)
            .body(request)
            .when()
            .put("/api/v1/artists/{id}")
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateArtist_ValidationError() {
        // First create an artist
        ArtistRequest createRequest = new ArtistRequest("Valid Name", ArtistType.BAND);

        Integer artistId = given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Try to update with invalid data
        ArtistRequest invalidRequest = new ArtistRequest("", ArtistType.BAND);

        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .pathParam("id", artistId)
            .body(invalidRequest)
            .when()
            .put("/api/v1/artists/{id}")
            .then()
            .statusCode(400);
    }

    @Test
    void testDeleteArtist_Success() {
        // First create an artist
        ArtistRequest createRequest = new ArtistRequest("Artist to Delete", ArtistType.SINGER);

        Integer artistId = given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .contentType(ContentType.JSON)
            .body(createRequest)
            .when()
            .post("/api/v1/artists")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then delete it
        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .pathParam("id", artistId)
            .when()
            .delete("/api/v1/artists/{id}")
            .then()
            .statusCode(204);

        // Verify it's deleted
        given()
            .auth().oauth2(TestTokenHelper.generateUserToken())
            .pathParam("id", artistId)
            .when()
            .get("/api/v1/artists/{id}")
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteArtist_NotFound() {
        given()
            .auth().oauth2(TestTokenHelper.generateAdminToken())
            .pathParam("id", 99999)
            .when()
            .delete("/api/v1/artists/{id}")
            .then()
            .statusCode(404);
    }
}
