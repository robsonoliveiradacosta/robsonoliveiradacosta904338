package com.quarkus.resource;

import com.quarkus.common.PostgresResource;
import com.quarkus.entity.Album;
import com.quarkus.util.TestTokenHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class FavoriteResourceTest {

    @Inject
    EntityManager entityManager;

    private Long albumA;
    private Long albumB;
    private Long albumC;

    @BeforeEach
    @Transactional
    void setUp() {
        entityManager.createQuery("DELETE FROM Favorite").executeUpdate();
        entityManager.createQuery("DELETE FROM Album").executeUpdate();

        Album a = new Album("Kind of Blue", 1959);
        Album b = new Album("A Love Supreme", 1965);
        Album c = new Album("Blue Train", 1957);
        entityManager.persist(a);
        entityManager.persist(b);
        entityManager.persist(c);
        entityManager.flush();
        albumA = a.getId();
        albumB = b.getId();
        albumC = c.getId();
    }

    // -- auth --

    @Test
    void shouldReturn401OnPostWithoutToken() {
        given().when().post("/api/v1/me/favorites/" + albumA).then().statusCode(401);
    }

    @Test
    void shouldReturn401OnDeleteWithoutToken() {
        given().when().delete("/api/v1/me/favorites/" + albumA).then().statusCode(401);
    }

    @Test
    void shouldReturn401OnListWithoutToken() {
        given().when().get("/api/v1/me/favorites").then().statusCode(401);
    }

    @Test
    void shouldReturn401OnStatusProbeWithoutToken() {
        given().when().get("/api/v1/me/favorites/" + albumA).then().statusCode(401);
    }

    // -- POST --

    @Test
    void shouldReturn201OnFirstFavorite() {
        given().auth().oauth2(TestTokenHelper.generateUserToken())
            .when().post("/api/v1/me/favorites/" + albumA)
            .then().statusCode(201)
            .body("albumId", equalTo(albumA.intValue()))
            .body("favoritedAt", notNullValue())
            .body("album.title", equalTo("Kind of Blue"));
    }

    @Test
    void shouldReturn200OnRepeatFavorite() {
        String token = TestTokenHelper.generateUserToken();

        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumA)
            .then().statusCode(201);

        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumA)
            .then().statusCode(200)
            .body("albumId", equalTo(albumA.intValue()));
    }

    @Test
    void shouldReturn404WhenFavoritingNonExistentAlbum() {
        given().auth().oauth2(TestTokenHelper.generateUserToken())
            .when().post("/api/v1/me/favorites/999999")
            .then().statusCode(404);
    }

    // -- DELETE --

    @Test
    void shouldReturn204OnDeleteWhetherOrNotFavoriteExisted() {
        String token = TestTokenHelper.generateUserToken();
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumA)
            .then().statusCode(201);

        given().auth().oauth2(token).when().delete("/api/v1/me/favorites/" + albumA)
            .then().statusCode(204);

        given().auth().oauth2(token).when().delete("/api/v1/me/favorites/" + albumA)
            .then().statusCode(204);
    }

    @Test
    void shouldReturn204OnDeleteForNonExistentAlbum() {
        given().auth().oauth2(TestTokenHelper.generateUserToken())
            .when().delete("/api/v1/me/favorites/999999")
            .then().statusCode(204);
    }

    // -- GET listing --

    @Test
    void shouldListOnlyMyFavoritesNotOthers() {
        // admin favorites albumA
        given().auth().oauth2(TestTokenHelper.generateAdminToken())
            .when().post("/api/v1/me/favorites/" + albumA).then().statusCode(201);

        // user GETs own list — should be empty
        given().auth().oauth2(TestTokenHelper.generateUserToken())
            .when().get("/api/v1/me/favorites")
            .then().statusCode(200)
            .body("totalElements", equalTo(0))
            .body("content", hasSize(0));

        // admin GETs own list — should have 1
        given().auth().oauth2(TestTokenHelper.generateAdminToken())
            .when().get("/api/v1/me/favorites")
            .then().statusCode(200)
            .body("totalElements", equalTo(1))
            .body("content[0].albumId", equalTo(albumA.intValue()));
    }

    @Test
    void shouldListNewestFirst() throws InterruptedException {
        String token = TestTokenHelper.generateUserToken();
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumA)
            .then().statusCode(201);
        Thread.sleep(20);
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumB)
            .then().statusCode(201);
        Thread.sleep(20);
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumC)
            .then().statusCode(201);

        given().auth().oauth2(token).when().get("/api/v1/me/favorites")
            .then().statusCode(200)
            .body("totalElements", equalTo(3))
            .body("content[0].albumId", equalTo(albumC.intValue()))
            .body("content[1].albumId", equalTo(albumB.intValue()))
            .body("content[2].albumId", equalTo(albumA.intValue()));
    }

    @Test
    void shouldRespectPageSizeAndPageParams() {
        String token = TestTokenHelper.generateUserToken();
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumA).then().statusCode(201);
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumB).then().statusCode(201);
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumC).then().statusCode(201);

        given().auth().oauth2(token)
            .queryParam("page", 0).queryParam("size", 2)
            .when().get("/api/v1/me/favorites")
            .then().statusCode(200)
            .body("size", equalTo(2))
            .body("totalElements", equalTo(3))
            .body("totalPages", equalTo(2))
            .body("content", hasSize(2));
    }

    // -- GET probe --

    @Test
    void shouldReturn200OnStatusProbeWhenFavorited() {
        String token = TestTokenHelper.generateUserToken();
        given().auth().oauth2(token).when().post("/api/v1/me/favorites/" + albumA).then().statusCode(201);

        given().auth().oauth2(token).when().get("/api/v1/me/favorites/" + albumA)
            .then().statusCode(200)
            .body("albumId", equalTo(albumA.intValue()))
            .body("favoritedAt", notNullValue());
    }

    @Test
    void shouldReturn404OnStatusProbeWhenNotFavorited() {
        given().auth().oauth2(TestTokenHelper.generateUserToken())
            .when().get("/api/v1/me/favorites/" + albumA)
            .then().statusCode(404);
    }

    @Test
    void shouldAllowAdminRoleSameAsUser() {
        given().auth().oauth2(TestTokenHelper.generateAdminToken())
            .when().post("/api/v1/me/favorites/" + albumA)
            .then().statusCode(201);
    }
}
