package com.quarkus.service;

import com.quarkus.dto.response.FavoriteResponse;
import com.quarkus.dto.response.FavoriteStatusResponse;
import com.quarkus.dto.response.PageResponse;
import com.quarkus.entity.Album;
import com.quarkus.entity.Favorite;
import com.quarkus.entity.User;
import com.quarkus.entity.UserRole;
import com.quarkus.repository.AlbumRepository;
import com.quarkus.repository.FavoriteRepository;
import com.quarkus.repository.UserRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock FavoriteRepository favoriteRepository;
    @Mock UserRepository userRepository;
    @Mock AlbumRepository albumRepository;

    @InjectMocks
    FavoriteService service;

    private User user;
    private Album album;

    @BeforeEach
    void setUp() {
        user = new User("alice", "hash", UserRole.USER);
        user.setId(7L);
        album = new Album("Kind of Blue", 1959);
        album.setId(42L);
    }

    @Test
    void shouldAddFavoriteAndPersist() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(albumRepository.findByIdOptional(42L)).thenReturn(Optional.of(album));
        when(favoriteRepository.findByUserAndAlbum(7L, 42L)).thenReturn(Optional.empty());

        FavoriteService.AddResult result = service.add("alice", 42L);

        assertTrue(result.created());
        assertEquals(42L, result.response().albumId());
        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        verify(favoriteRepository).persist(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertSame(album, captor.getValue().getAlbum());
    }

    @Test
    void shouldReturn200SemanticsWhenFavoriteAlreadyExists() {
        Favorite existing = persistedFavorite();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(albumRepository.findByIdOptional(42L)).thenReturn(Optional.of(album));
        when(favoriteRepository.findByUserAndAlbum(7L, 42L)).thenReturn(Optional.of(existing));

        FavoriteService.AddResult result = service.add("alice", 42L);

        assertFalse(result.created());
        assertEquals(42L, result.response().albumId());
        verify(favoriteRepository, never()).persist(any(Favorite.class));
    }

    @Test
    void shouldThrowNotFoundWhenAlbumDoesNotExistOnAdd() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(albumRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.add("alice", 999L));
        verify(favoriteRepository, never()).persist(any(Favorite.class));
    }

    @Test
    void shouldHandleRaceByReReadingExistingRow() {
        Favorite winner = persistedFavorite();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(albumRepository.findByIdOptional(42L)).thenReturn(Optional.of(album));
        // First check: empty (we lost the race)
        // Re-read after constraint violation: present
        when(favoriteRepository.findByUserAndAlbum(7L, 42L))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(winner));

        ConstraintViolationException cve =
            new ConstraintViolationException("uq", new SQLException(), "uq_favorites_user_album");
        doThrow(new PersistenceException(cve)).when(favoriteRepository).flush();

        FavoriteService.AddResult result = service.add("alice", 42L);

        assertFalse(result.created());
        assertEquals(42L, result.response().albumId());
        verify(favoriteRepository, times(2)).findByUserAndAlbum(7L, 42L);
    }

    @Test
    void shouldRemoveFavoriteByUserAndAlbum() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(favoriteRepository.deleteByUserAndAlbum(7L, 42L)).thenReturn(1L);

        service.remove("alice", 42L);

        verify(favoriteRepository).deleteByUserAndAlbum(7L, 42L);
    }

    @Test
    void shouldBeNoOpWhenRemovingNonExistentFavorite() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(favoriteRepository.deleteByUserAndAlbum(7L, 42L)).thenReturn(0L);

        assertDoesNotThrow(() -> service.remove("alice", 42L));
    }

    @Test
    void shouldListFavoritesScopedToCurrentUser() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserPaged(eq(7L), any(Page.class), any(Sort.class)))
            .thenReturn(List.of(persistedFavorite()));
        when(favoriteRepository.countByUser(7L)).thenReturn(1L);

        PageResponse<FavoriteResponse> result = service.list("alice", 0, 20, null);

        assertEquals(1, result.totalElements());
        assertEquals(1, result.content().size());
        verify(favoriteRepository).findByUserPaged(eq(7L), any(Page.class), any(Sort.class));
    }

    @Test
    void shouldClampPageSizeAt100AndMin1() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserPaged(eq(7L), any(Page.class), any(Sort.class)))
            .thenReturn(List.of());
        when(favoriteRepository.countByUser(7L)).thenReturn(0L);

        PageResponse<FavoriteResponse> tooLarge = service.list("alice", 0, 500, null);
        assertEquals(100, tooLarge.size());

        PageResponse<FavoriteResponse> tooSmall = service.list("alice", 0, 0, null);
        assertEquals(20, tooSmall.size());

        PageResponse<FavoriteResponse> negativePage = service.list("alice", -1, 20, null);
        assertEquals(0, negativePage.page());
    }

    @Test
    void shouldReturnFavoriteStatusWhenFavorited() {
        Favorite existing = persistedFavorite();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserAndAlbum(7L, 42L)).thenReturn(Optional.of(existing));

        FavoriteStatusResponse status = service.get("alice", 42L);

        assertEquals(42L, status.albumId());
        assertNotNull(status.favoritedAt());
    }

    @Test
    void shouldThrowNotFoundOnStatusProbeWhenNotFavorited() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(favoriteRepository.findByUserAndAlbum(7L, 42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get("alice", 42L));
    }

    @Test
    void shouldRejectMissingPrincipal() {
        assertThrows(NotAuthorizedException.class, () -> service.add(null, 42L));
        assertThrows(NotAuthorizedException.class, () -> service.add("", 42L));
    }

    private Favorite persistedFavorite() {
        Favorite f = new Favorite(user, album);
        f.setId(100L);
        f.setCreatedAt(Instant.parse("2026-05-15T10:30:00Z"));
        return f;
    }
}
