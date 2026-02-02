package com.quarkus.resource;

import com.quarkus.common.PostgresResource;
import com.quarkus.dto.request.AlbumRequest;
import com.quarkus.entity.Album;
import com.quarkus.entity.Artist;
import com.quarkus.entity.ArtistType;
import com.quarkus.util.TestTokenHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class AlbumResourceTest {

    @Inject
    EntityManager entityManager;

    private Long artist1Id;
    private Long artist2Id;
    private Long album1Id;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        entityManager.createQuery("DELETE FROM Album").executeUpdate();
        entityManager.createQuery("DELETE FROM Artist").executeUpdate();

        // Create test artists
        Artist artist1 = new Artist("Queen", ArtistType.BAND);
        entityManager.persist(artist1);
        artist1Id = artist1.getId();

        Artist artist2 = new Artist("Freddie Mercury", ArtistType.SINGER);
        entityManager.persist(artist2);
        artist2Id = artist2.getId();

        // Create test album
        Album album1 = new Album("A Night at the Opera", 1975);
        album1.setArtists(Set.of(artist1));
        entityManager.persist(album1);
        album1Id = album1.getId();

        entityManager.flush();
    }

    @Test
    void shouldListAllAlbums() {
        given()
                .when()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .get("/api/v1/albums")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].title", equalTo("A Night at the Opera"))
                .body("content[0].year", equalTo(1975))
                .body("content[0].artists", hasSize(1))
                .body("page", equalTo(0))
                .body("size", equalTo(20))
                .body("totalElements", equalTo(1))
                .body("totalPages", equalTo(1));
    }

    @Test
    void shouldListAlbumsWithPagination() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/api/v1/albums")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("page", equalTo(0))
                .body("size", equalTo(1))
                .body("totalElements", equalTo(1));
    }

    @Test
    void shouldListAlbumsWithSorting() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .queryParam("sort", "title:desc")
                .when()
                .get("/api/v1/albums")
                .then()
                .statusCode(200)
                .body("content", hasSize(1));
    }

    @Test
    void shouldFilterAlbumsByArtistType() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .queryParam("artistType", "BAND")
                .when()
                .get("/api/v1/albums")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].artists[0].type", equalTo("BAND"));
    }

    @Test
    void shouldReturnEmptyWhenFilteringByNonMatchingArtistType() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .queryParam("artistType", "SINGER")
                .when()
                .get("/api/v1/albums")
                .then()
                .statusCode(200)
                .body("content", hasSize(0));
    }

    @Test
    void shouldGetAlbumById() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .pathParam("id", album1Id)
                .when()
                .get("/api/v1/albums/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(album1Id.intValue()))
                .body("title", equalTo("A Night at the Opera"))
                .body("year", equalTo(1975))
                .body("artists", hasSize(1));
    }

    @Test
    void shouldReturn404WhenAlbumNotFound() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .pathParam("id", 99999)
                .when()
                .get("/api/v1/albums/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldCreateAlbum() {
        AlbumRequest request = new AlbumRequest("Bohemian Rhapsody", 1975, List.of(artist1Id, artist2Id));

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .post("/api/v1/albums")
                .then()
                .statusCode(201)
                .body("title", equalTo("Bohemian Rhapsody"))
                .body("year", equalTo(1975))
                .body("artists", hasSize(2));
    }

    @Test
    void shouldReturn400WhenCreatingAlbumWithInvalidData() {
        AlbumRequest request = new AlbumRequest("", null, List.of());

        given()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/albums")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn404WhenCreatingAlbumWithNonExistentArtist() {
        AlbumRequest request = new AlbumRequest("Test Album", 2020, List.of(99999L));

        given()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/albums")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldUpdateAlbum() {
        AlbumRequest request = new AlbumRequest("Updated Title", 1976, List.of(artist2Id));

        given()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .pathParam("id", album1Id)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/v1/albums/{id}")
                .then()
                .statusCode(200)
                .body("id", equalTo(album1Id.intValue()))
                .body("title", equalTo("Updated Title"))
                .body("year", equalTo(1976))
                .body("artists", hasSize(1))
                .body("artists[0].id", equalTo(artist2Id.intValue()));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentAlbum() {
        AlbumRequest request = new AlbumRequest("Test", 2020, List.of(artist1Id));

        given()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .pathParam("id", 99999)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/v1/albums/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenUpdatingAlbumWithInvalidData() {
        AlbumRequest request = new AlbumRequest("", null, List.of());

        given()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .pathParam("id", album1Id)
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/api/v1/albums/{id}")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldDeleteAlbum() {
        given()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .pathParam("id", album1Id)
                .when()
                .delete("/api/v1/albums/{id}")
                .then()
                .statusCode(204);

        // Verify album is deleted
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .pathParam("id", album1Id)
                .when()
                .get("/api/v1/albums/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentAlbum() {
        given()
                .auth().oauth2(TestTokenHelper.generateAdminToken())
                .pathParam("id", 99999)
                .when()
                .delete("/api/v1/albums/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void shouldEnforcePaginationLimits() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .queryParam("size", 150) // Over max of 100
                .when()
                .get("/api/v1/albums")
                .then()
                .statusCode(200)
                .body("size", equalTo(100)); // Should be capped at 100
    }

    @Test
    void shouldHandleInvalidSortParameter() {
        given()
                .auth().oauth2(TestTokenHelper.generateUserToken())
                .queryParam("sort", "invalid:param")
                .when()
                .get("/api/v1/albums")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }
}
