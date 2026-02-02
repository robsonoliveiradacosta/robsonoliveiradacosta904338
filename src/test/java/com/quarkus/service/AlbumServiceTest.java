package com.quarkus.service;

import com.quarkus.dto.request.AlbumRequest;
import com.quarkus.dto.response.AlbumResponse;
import com.quarkus.dto.response.PageResponse;
import com.quarkus.entity.Album;
import com.quarkus.entity.Artist;
import com.quarkus.entity.ArtistType;
import com.quarkus.repository.AlbumRepository;
import com.quarkus.repository.ArtistRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    AlbumRepository albumRepository;

    @Mock
    ArtistRepository artistRepository;

    @InjectMocks
    AlbumService albumService;

    private Artist artist1;
    private Artist artist2;
    private Album album1;

    @BeforeEach
    void setUp() {
        artist1 = new Artist("Queen", ArtistType.BAND);
        artist1.setId(1L);

        artist2 = new Artist("Freddie Mercury", ArtistType.SINGER);
        artist2.setId(2L);

        album1 = new Album("A Night at the Opera", 1975);
        album1.setId(1L);
        album1.setArtists(Set.of(artist1));
    }

    @Test
    void shouldFindAllAlbumsWithPagination() {
        // Given
        List<Album> albums = List.of(album1);
        when(albumRepository.findWithFilters(any(Page.class), any(Sort.class), isNull()))
            .thenReturn(albums);
        when(albumRepository.countWithFilters(isNull())).thenReturn(1L);

        // When
        PageResponse<AlbumResponse> result = albumService.findAll(0, 20, "title:asc", null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1L, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals("A Night at the Opera", result.content().get(0).title());

        verify(albumRepository).findWithFilters(any(Page.class), any(Sort.class), isNull());
        verify(albumRepository).countWithFilters(isNull());
    }

    @Test
    void shouldCapPageSizeAt100() {
        // Given
        when(albumRepository.findWithFilters(any(Page.class), any(Sort.class), isNull()))
            .thenReturn(List.of());
        when(albumRepository.countWithFilters(isNull())).thenReturn(0L);

        // When
        albumService.findAll(0, 150, null, null);

        // Then
        verify(albumRepository).findWithFilters(argThat(page -> page.size == 100), any(Sort.class), isNull());
    }

    @Test
    void shouldFilterByArtistType() {
        // Given
        when(albumRepository.findWithFilters(any(Page.class), any(Sort.class), eq(ArtistType.BAND)))
            .thenReturn(List.of(album1));
        when(albumRepository.countWithFilters(eq(ArtistType.BAND))).thenReturn(1L);

        // When
        PageResponse<AlbumResponse> result = albumService.findAll(0, 20, null, ArtistType.BAND);

        // Then
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(albumRepository).findWithFilters(any(Page.class), any(Sort.class), eq(ArtistType.BAND));
        verify(albumRepository).countWithFilters(eq(ArtistType.BAND));
    }

    @Test
    void shouldFindAlbumById() {
        // Given
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(album1));

        // When
        AlbumResponse result = albumService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("A Night at the Opera", result.title());
        assertEquals(1975, result.year());
        assertEquals(1, result.artists().size());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAlbumNotFound() {
        // Given
        when(albumRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> albumService.findById(999L));
    }

    @Test
    void shouldCreateAlbumWithArtists() {
        // Given
        AlbumRequest request = new AlbumRequest("Bohemian Rhapsody", 1975, List.of(1L, 2L));
        when(artistRepository.findByIdOptional(1L)).thenReturn(Optional.of(artist1));
        when(artistRepository.findByIdOptional(2L)).thenReturn(Optional.of(artist2));
        doAnswer(invocation -> {
            Album album = invocation.getArgument(0);
            album.setId(10L);
            return null;
        }).when(albumRepository).persist(any(Album.class));

        // When
        AlbumResponse result = albumService.create(request);

        // Then
        assertNotNull(result);
        assertEquals("Bohemian Rhapsody", result.title());
        assertEquals(1975, result.year());
        assertEquals(2, result.artists().size());

        verify(artistRepository).findByIdOptional(1L);
        verify(artistRepository).findByIdOptional(2L);
        verify(albumRepository).persist(any(Album.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenArtistNotFoundDuringCreate() {
        // Given
        AlbumRequest request = new AlbumRequest("Test Album", 2020, List.of(999L));
        when(artistRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> albumService.create(request));
    }

    @Test
    void shouldUpdateAlbum() {
        // Given
        AlbumRequest request = new AlbumRequest("Updated Title", 1976, List.of(2L));
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(album1));
        when(artistRepository.findByIdOptional(2L)).thenReturn(Optional.of(artist2));

        // When
        AlbumResponse result = albumService.update(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Title", result.title());
        assertEquals(1976, result.year());
        assertEquals(1, result.artists().size());
        assertEquals(2L, result.artists().get(0).id());

        verify(albumRepository).findByIdOptional(1L);
        verify(artistRepository).findByIdOptional(2L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentAlbum() {
        // Given
        AlbumRequest request = new AlbumRequest("Test", 2020, List.of(1L));
        when(albumRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> albumService.update(999L, request));
    }

    @Test
    void shouldDeleteAlbum() {
        // Given
        when(albumRepository.findByIdOptional(1L)).thenReturn(Optional.of(album1));

        // When
        albumService.delete(1L);

        // Then
        verify(albumRepository).findByIdOptional(1L);
        verify(albumRepository).delete(album1);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentAlbum() {
        // Given
        when(albumRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> albumService.delete(999L));
    }

    @Test
    void shouldParseSortParamCorrectly() {
        // Given
        when(albumRepository.findWithFilters(any(Page.class), any(Sort.class), isNull()))
            .thenReturn(List.of());
        when(albumRepository.countWithFilters(isNull())).thenReturn(0L);

        // When - test various sort parameters
        albumService.findAll(0, 20, "title:asc", null);
        albumService.findAll(0, 20, "year:desc", null);
        albumService.findAll(0, 20, null, null);
        albumService.findAll(0, 20, "invalid", null);

        // Then
        verify(albumRepository, times(4)).findWithFilters(any(Page.class), any(Sort.class), isNull());
    }
}
