package com.quarkus.service;

import com.quarkus.dto.request.AlbumRequest;
import com.quarkus.dto.response.AlbumResponse;
import com.quarkus.dto.response.PageResponse;
import com.quarkus.entity.Album;
import com.quarkus.entity.Artist;
import com.quarkus.entity.ArtistType;
import com.quarkus.repository.AlbumRepository;
import com.quarkus.repository.ArtistRepository;
import com.quarkus.websocket.AlbumNotificationSocket;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class AlbumService {

    @Inject
    AlbumRepository albumRepository;

    @Inject
    ArtistRepository artistRepository;

    @Inject
    AlbumNotificationSocket notificationSocket;

    /**
     * Find all albums with pagination, sorting and optional artist type filter.
     *
     * @param page Page number (0-based)
     * @param size Page size (max 100)
     * @param sortParam Sort parameter (e.g., "title:asc", "year:desc")
     * @param artistType Optional artist type filter
     * @return Paginated album response
     */
    public PageResponse<AlbumResponse> findAll(int page, int size, String sortParam, ArtistType artistType) {
        // Validate and cap page size
        if (size > 100) {
            size = 100;
        }
        if (size <= 0) {
            size = 20;
        }
        if (page < 0) {
            page = 0;
        }

        // Parse sort parameter
        Sort sort = parseSortParam(sortParam);

        // Query with filters
        Page pageRequest = Page.of(page, size);
        List<Album> albums = albumRepository.findWithFilters(pageRequest, sort, artistType);
        long totalElements = albumRepository.countWithFilters(artistType);

        // Convert to response DTOs
        List<AlbumResponse> content = albums.stream()
            .map(AlbumResponse::from)
            .toList();

        return PageResponse.of(content, page, size, totalElements);
    }

    /**
     * Find album by ID.
     *
     * @param id Album ID
     * @return Album response
     * @throws NotFoundException if album not found
     */
    public AlbumResponse findById(Long id) {
        Album album = albumRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Album not found with id: " + id));
        return AlbumResponse.from(album);
    }

    /**
     * Create a new album with linked artists.
     *
     * @param request Album creation request
     * @return Created album response
     * @throws NotFoundException if any artist ID is not found
     */
    @Transactional
    public AlbumResponse create(AlbumRequest request) {
        // Load artists
        Set<Artist> artists = loadArtists(request.artistIds());

        // Create album
        Album album = new Album(request.title(), request.year());
        album.setArtists(artists);

        albumRepository.persist(album);

        // Notify WebSocket clients
        notificationSocket.notifyNewAlbum(album);

        return AlbumResponse.from(album);
    }

    /**
     * Update an existing album.
     *
     * @param id Album ID
     * @param request Album update request
     * @return Updated album response
     * @throws NotFoundException if album or any artist not found
     */
    @Transactional
    public AlbumResponse update(Long id, AlbumRequest request) {
        Album album = albumRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Album not found with id: " + id));

        // Load new artists
        Set<Artist> artists = loadArtists(request.artistIds());

        // Update fields
        album.setTitle(request.title());
        album.setYear(request.year());
        album.setArtists(artists);

        return AlbumResponse.from(album);
    }

    /**
     * Delete an album (hard delete).
     *
     * @param id Album ID
     * @throws NotFoundException if album not found
     */
    @Transactional
    public void delete(Long id) {
        Album album = albumRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Album not found with id: " + id));
        albumRepository.delete(album);
    }

    /**
     * Load artists by IDs and validate all exist.
     *
     * @param artistIds List of artist IDs
     * @return Set of loaded artists
     * @throws NotFoundException if any artist ID is not found
     */
    private Set<Artist> loadArtists(List<Long> artistIds) {
        Set<Artist> artists = new HashSet<>();
        for (Long artistId : artistIds) {
            Artist artist = artistRepository.findByIdOptional(artistId)
                .orElseThrow(() -> new NotFoundException("Artist not found with id: " + artistId));
            artists.add(artist);
        }
        return artists;
    }

    /**
     * Parse sort parameter string into Sort object.
     * Format: "field:direction" (e.g., "title:asc", "year:desc")
     * Default: "title:asc"
     *
     * @param sortParam Sort parameter string
     * @return Sort object
     */
    private Sort parseSortParam(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by("title").ascending();
        }

        String[] parts = sortParam.split(":");
        if (parts.length != 2) {
            return Sort.by("title").ascending();
        }

        String field = parts[0].trim();
        String direction = parts[1].trim().toLowerCase();

        // Validate field
        if (!field.equals("title") && !field.equals("year")) {
            field = "title";
        }

        // Apply direction
        if ("desc".equals(direction)) {
            return Sort.by(field).descending();
        }
        return Sort.by(field).ascending();
    }
}
