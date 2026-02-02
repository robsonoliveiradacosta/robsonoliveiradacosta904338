package com.quarkus.websocket;

import com.quarkus.entity.Album;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@WebSocket(path = "/ws/albums")
@ApplicationScoped
public class AlbumNotificationSocket {

    @Inject
    WebSocketConnection connection;

    private static final BroadcastProcessor<AlbumNotification> broadcaster = BroadcastProcessor.create();

    public record AlbumNotification(Long id, String title, Integer year) {}

    @OnOpen
    public Multi<AlbumNotification> onOpen() {
        Log.infof("Client connected: %s", connection.id());
        return broadcaster;
    }

    @OnClose
    public void onClose() {
        Log.infof("Client disconnected: %s", connection.id());
    }

    public void notifyNewAlbum(Album album) {
        AlbumNotification notification = new AlbumNotification(
            album.getId(),
            album.getTitle(),
            album.getYear()
        );

        broadcaster.onNext(notification);
        Log.infof("Broadcast sent for album: %s", album.getTitle());
    }
}
