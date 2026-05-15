package com.quarkus.service;

import com.quarkus.dto.response.FavoriteResponse;
import com.quarkus.dto.response.FavoriteStatusResponse;
import com.quarkus.dto.response.PageResponse;
import com.quarkus.entity.Album;
import com.quarkus.entity.Favorite;
import com.quarkus.entity.User;
import com.quarkus.repository.AlbumRepository;
import com.quarkus.repository.FavoriteRepository;
import com.quarkus.repository.UserRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;

@ApplicationScoped
public class FavoriteService {

    @Inject
    FavoriteRepository favoriteRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    AlbumRepository albumRepository;

    /**
     * Result of an add operation. {@code created} is true when a new
     * favorite row was inserted, false when it already existed (idempotent).
     */
    public record AddResult(FavoriteResponse response, boolean created) {
    }

    @Transactional
    public AddResult add(String username, Long albumId) {
        User user = requireUser(username);
        Album album = albumRepository.findByIdOptional(albumId)
            .orElseThrow(() -> new NotFoundException("Album not found with id: " + albumId));

        var existing = favoriteRepository.findByUserAndAlbum(user.getId(), albumId);
        if (existing.isPresent()) {
            return new AddResult(FavoriteResponse.from(existing.get()), false);
        }

        Favorite favorite = new Favorite(user, album);
        try {
            favoriteRepository.persist(favorite);
            favoriteRepository.flush();
        } catch (PersistenceException e) {
            // Concurrent insert won the race — re-read and treat as already-favorited.
            if (isUniqueViolation(e)) {
                Favorite existingFromRace = favoriteRepository
                    .findByUserAndAlbum(user.getId(), albumId)
                    .orElseThrow(() -> e);
                return new AddResult(FavoriteResponse.from(existingFromRace), false);
            }
            throw e;
        }
        return new AddResult(FavoriteResponse.from(favorite), true);
    }

    @Transactional
    public void remove(String username, Long albumId) {
        User user = requireUser(username);
        favoriteRepository.deleteByUserAndAlbum(user.getId(), albumId);
    }

    public PageResponse<FavoriteResponse> list(String username, int page, int size, String sortParam) {
        User user = requireUser(username);

        if (size > 100) size = 100;
        if (size <= 0) size = 20;
        if (page < 0) page = 0;

        Sort sort = parseSortParam(sortParam);
        Page pageRequest = Page.of(page, size);

        List<Favorite> rows = favoriteRepository.findByUserPaged(user.getId(), pageRequest, sort);
        long total = favoriteRepository.countByUser(user.getId());

        List<FavoriteResponse> content = rows.stream()
            .map(FavoriteResponse::from)
            .toList();

        return PageResponse.of(content, page, size, total);
    }

    public FavoriteStatusResponse get(String username, Long albumId) {
        User user = requireUser(username);
        Favorite favorite = favoriteRepository.findByUserAndAlbum(user.getId(), albumId)
            .orElseThrow(() -> new NotFoundException(
                "Album " + albumId + " is not favorited"));
        return new FavoriteStatusResponse(favorite.getAlbum().getId(), favorite.getCreatedAt());
    }

    private User requireUser(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedException("No authenticated user");
        }
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new NotAuthorizedException(
                "Authenticated principal not found in users table: " + username));
    }

    /**
     * Sort whitelist: only {@code createdAt} (asc/desc). Default desc.
     */
    private Sort parseSortParam(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by("createdAt").descending();
        }
        String[] parts = sortParam.split(":");
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim().toLowerCase() : "desc";

        if (!"createdAt".equals(field)) {
            field = "createdAt";
        }
        return "asc".equals(direction)
            ? Sort.by(field).ascending()
            : Sort.by(field).descending();
    }

    private boolean isUniqueViolation(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof ConstraintViolationException) return true;
            cur = cur.getCause();
        }
        return false;
    }
}
