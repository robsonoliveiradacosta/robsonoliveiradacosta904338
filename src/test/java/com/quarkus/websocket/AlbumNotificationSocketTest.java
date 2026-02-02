package com.quarkus.websocket;

import com.quarkus.common.PostgresResource;
import com.quarkus.dto.request.AlbumRequest;
import com.quarkus.entity.Artist;
import com.quarkus.entity.ArtistType;
import com.quarkus.util.TestTokenHelper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.websocket.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
class AlbumNotificationSocketTest {

    private static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();

    @TestHTTPResource("/ws/albums")
    URI uri;

    @Inject
    EntityManager entityManager;

    private Long artistId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up messages from previous tests
        MESSAGES.clear();

        // Clean up existing data
        entityManager.createQuery("DELETE FROM Album").executeUpdate();
        entityManager.createQuery("DELETE FROM Artist").executeUpdate();

        // Create test artist
        Artist artist = new Artist("Test Artist", ArtistType.BAND);
        entityManager.persist(artist);
        artistId = artist.getId();
    }

    @Test
    void shouldReceiveBroadcastWhenAlbumIsCreated() throws Exception {
        // Connect WebSocket client
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            // Wait for connection
            String connectMessage = MESSAGES.poll(5, TimeUnit.SECONDS);
            Assertions.assertEquals("CONNECT", connectMessage, "Should receive CONNECT message");

            // Create album via REST API (this should trigger WebSocket broadcast)
            String token = TestTokenHelper.generateAdminToken();
            AlbumRequest request = new AlbumRequest("New Album", 2024, List.of(artistId));

            given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(request)
            .when()
                .post("/api/v1/albums")
            .then()
                .statusCode(201)
                .body("id", notNullValue());

            // Wait for WebSocket notification
            String notification = MESSAGES.poll(10, TimeUnit.SECONDS);
            Assertions.assertNotNull(notification, "Should receive album notification");
            Assertions.assertTrue(notification.contains("\"title\":\"New Album\""),
                "Notification should contain album title");
            Assertions.assertTrue(notification.contains("\"year\":2024"),
                "Notification should contain album year");
        }
    }

    @Test
    void shouldHandleMultipleClientsConnection() throws Exception {
        // Connect first WebSocket client
        try (Session session1 = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            String connect1 = MESSAGES.poll(5, TimeUnit.SECONDS);
            Assertions.assertEquals("CONNECT", connect1);

            // Connect second WebSocket client
            try (Session session2 = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
                String connect2 = MESSAGES.poll(5, TimeUnit.SECONDS);
                Assertions.assertEquals("CONNECT", connect2);

                // Create album via REST API
                String token = TestTokenHelper.generateAdminToken();
                AlbumRequest request = new AlbumRequest("Multi-Client Album", 2024, List.of(artistId));

                given()
                    .auth().oauth2(token)
                    .contentType(ContentType.JSON)
                    .body(request)
                .when()
                    .post("/api/v1/albums")
                .then()
                    .statusCode(201);

                // Both clients should receive the notification
                String notification1 = MESSAGES.poll(10, TimeUnit.SECONDS);
                String notification2 = MESSAGES.poll(10, TimeUnit.SECONDS);

                Assertions.assertNotNull(notification1, "First client should receive notification");
                Assertions.assertNotNull(notification2, "Second client should receive notification");

                // Both notifications should contain the album title
                Assertions.assertTrue(notification1.contains("\"title\":\"Multi-Client Album\""));
                Assertions.assertTrue(notification2.contains("\"title\":\"Multi-Client Album\""));
            }
        }
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(Session session) {
            MESSAGES.add("CONNECT");
        }

        @OnMessage
        public void message(String msg) {
            MESSAGES.add(msg);
        }

        @OnError
        public void error(Session session, Throwable throwable) {
            MESSAGES.add("ERROR: " + throwable.getMessage());
        }
    }
}
