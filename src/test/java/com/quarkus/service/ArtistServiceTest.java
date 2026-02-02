package com.quarkus.service;

import com.quarkus.dto.request.ArtistRequest;
import com.quarkus.dto.response.ArtistResponse;
import com.quarkus.entity.Artist;
import com.quarkus.entity.ArtistType;
import com.quarkus.repository.ArtistRepository;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ArtistServiceTest {

    @Inject
    ArtistService artistService;

    @InjectMock
    ArtistRepository artistRepository;

    @Test
    void testListArtists_WithNoFilters() {
        // Given
        Artist artist1 = new Artist("Queen", ArtistType.BAND);
        artist1.setId(1L);
        Artist artist2 = new Artist("Michael Jackson", ArtistType.SINGER);
        artist2.setId(2L);

        when(artistRepository.findByNameContaining(isNull(), any(Sort.class)))
            .thenReturn(Arrays.asList(artist1, artist2));

        // When
        List<ArtistResponse> result = artistService.listArtists(null, null);

        // Then
        assertEquals(2, result.size());
        assertEquals("Queen", result.get(0).name());
        assertEquals(ArtistType.BAND, result.get(0).type());
        verify(artistRepository).findByNameContaining(isNull(), any(Sort.class));
    }

    @Test
    void testListArtists_WithNameFilter() {
        // Given
        Artist artist = new Artist("Queen", ArtistType.BAND);
        artist.setId(1L);

        when(artistRepository.findByNameContaining(eq("Queen"), any(Sort.class)))
            .thenReturn(Arrays.asList(artist));

        // When
        List<ArtistResponse> result = artistService.listArtists("Queen", null);

        // Then
        assertEquals(1, result.size());
        assertEquals("Queen", result.get(0).name());
        verify(artistRepository).findByNameContaining(eq("Queen"), any(Sort.class));
    }

    @Test
    void testListArtists_WithSortDescending() {
        // Given
        Artist artist1 = new Artist("Queen", ArtistType.BAND);
        artist1.setId(1L);

        when(artistRepository.findByNameContaining(isNull(), any(Sort.class)))
            .thenReturn(Arrays.asList(artist1));

        // When
        List<ArtistResponse> result = artistService.listArtists(null, "name:desc");

        // Then
        assertEquals(1, result.size());
        verify(artistRepository).findByNameContaining(isNull(), any(Sort.class));
    }

    @Test
    void testFindById_Success() {
        // Given
        Artist artist = new Artist("Queen", ArtistType.BAND);
        artist.setId(1L);

        when(artistRepository.findByIdOptional(1L)).thenReturn(Optional.of(artist));

        // When
        ArtistResponse result = artistService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Queen", result.name());
        assertEquals(ArtistType.BAND, result.type());
        verify(artistRepository).findByIdOptional(1L);
    }

    @Test
    void testFindById_NotFound() {
        // Given
        when(artistRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> artistService.findById(999L));
        verify(artistRepository).findByIdOptional(999L);
    }

    @Test
    void testCreateArtist() {
        // Given
        ArtistRequest request = new ArtistRequest("The Beatles", ArtistType.BAND);

        doAnswer(invocation -> {
            Artist arg = invocation.getArgument(0);
            arg.setId(1L);
            return null;
        }).when(artistRepository).persist(any(Artist.class));

        // When
        ArtistResponse result = artistService.createArtist(request);

        // Then
        assertNotNull(result);
        assertEquals("The Beatles", result.name());
        assertEquals(ArtistType.BAND, result.type());
        verify(artistRepository).persist(any(Artist.class));
    }

    @Test
    void testUpdateArtist_Success() {
        // Given
        Artist existingArtist = new Artist("Queen", ArtistType.BAND);
        existingArtist.setId(1L);

        ArtistRequest updateRequest = new ArtistRequest("Queen (Updated)", ArtistType.BAND);

        when(artistRepository.findByIdOptional(1L)).thenReturn(Optional.of(existingArtist));

        // When
        ArtistResponse result = artistService.updateArtist(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Queen (Updated)", result.name());
        assertEquals(ArtistType.BAND, result.type());
        verify(artistRepository).findByIdOptional(1L);
    }

    @Test
    void testUpdateArtist_NotFound() {
        // Given
        ArtistRequest request = new ArtistRequest("Queen", ArtistType.BAND);
        when(artistRepository.findByIdOptional(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> artistService.updateArtist(999L, request));
        verify(artistRepository).findByIdOptional(999L);
    }

    @Test
    void testDeleteArtist_Success() {
        // Given
        when(artistRepository.deleteById(1L)).thenReturn(true);

        // When
        artistService.deleteArtist(1L);

        // Then
        verify(artistRepository).deleteById(1L);
    }

    @Test
    void testDeleteArtist_NotFound() {
        // Given
        when(artistRepository.deleteById(999L)).thenReturn(false);

        // When & Then
        assertThrows(NotFoundException.class, () -> artistService.deleteArtist(999L));
        verify(artistRepository).deleteById(999L);
    }
}
