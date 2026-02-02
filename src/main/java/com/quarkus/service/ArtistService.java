package com.quarkus.service;

import com.quarkus.dto.request.ArtistRequest;
import com.quarkus.dto.response.ArtistResponse;
import com.quarkus.entity.Artist;
import com.quarkus.repository.ArtistRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArtistService {

    @Inject
    ArtistRepository artistRepository;

    /**
     * List all artists with optional name filter and sorting.
     *
     * @param name Optional name filter
     * @param sortParam Sort parameter in format "field:direction" (e.g., "name:asc")
     * @return List of artist responses
     */
    public List<ArtistResponse> listArtists(String name, String sortParam) {
        Sort sort = parseSortParameter(sortParam);
        return artistRepository.findByNameContaining(name, sort)
            .stream()
            .map(ArtistResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * Find artist by ID.
     *
     * @param id Artist ID
     * @return Artist response
     * @throws NotFoundException if artist not found
     */
    public ArtistResponse findById(Long id) {
        Artist artist = artistRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Artist not found with id: " + id));
        return ArtistResponse.from(artist);
    }

    /**
     * Create a new artist.
     *
     * @param request Artist creation request
     * @return Created artist response
     */
    @Transactional
    public ArtistResponse createArtist(ArtistRequest request) {
        Artist artist = new Artist(request.name(), request.type());
        artistRepository.persist(artist);
        return ArtistResponse.from(artist);
    }

    /**
     * Update an existing artist.
     *
     * @param id Artist ID
     * @param request Artist update request
     * @return Updated artist response
     * @throws NotFoundException if artist not found
     */
    @Transactional
    public ArtistResponse updateArtist(Long id, ArtistRequest request) {
        Artist artist = artistRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Artist not found with id: " + id));

        artist.setName(request.name());
        artist.setType(request.type());

        return ArtistResponse.from(artist);
    }

    /**
     * Delete an artist by ID (hard delete).
     *
     * @param id Artist ID
     * @throws NotFoundException if artist not found
     */
    @Transactional
    public void deleteArtist(Long id) {
        boolean deleted = artistRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Artist not found with id: " + id);
        }
    }

    /**
     * Parse sort parameter in format "field:direction".
     * Defaults to "name:asc" if null or invalid.
     *
     * @param sortParam Sort parameter string
     * @return Sort object
     */
    private Sort parseSortParameter(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by("name").ascending();
        }

        String[] parts = sortParam.split(":");
        String field = parts.length > 0 ? parts[0] : "name";
        String direction = parts.length > 1 ? parts[1] : "asc";

        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.by(field).descending();
        }
        return Sort.by(field).ascending();
    }
}
