package com.quarkus.repository;

import com.quarkus.entity.Favorite;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FavoriteRepository implements PanacheRepository<Favorite> {

    public Optional<Favorite> findByUserAndAlbum(Long userId, Long albumId) {
        return find("user.id = ?1 and album.id = ?2", userId, albumId).firstResultOptional();
    }

    /**
     * List favorites for a user, eagerly fetching the album (and its artists).
     * Images are intentionally NOT fetched — AlbumResponse doesn't include them.
     *
     * <p>Two-query pattern: page IDs first (real SQL LIMIT/OFFSET, no collection
     * fetch), then hydrate the page with JOIN FETCH. Combining JOIN FETCH of a
     * @ManyToMany collection with .page() in a single query forces Hibernate to
     * paginate in memory (HHH000104).
     */
    public List<Favorite> findByUserPaged(Long userId, Page page, Sort sort) {
        List<Long> ids = find("user.id = ?1", sort, userId)
            .page(page)
            .list()
            .stream()
            .map(Favorite::getId)
            .toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        return find("SELECT DISTINCT f FROM Favorite f "
                  + "JOIN FETCH f.album a "
                  + "LEFT JOIN FETCH a.artists "
                  + "WHERE f.id IN ?1", sort, ids)
            .list();
    }

    public long countByUser(Long userId) {
        return count("user.id", userId);
    }

    public long deleteByUserAndAlbum(Long userId, Long albumId) {
        return delete("user.id = ?1 and album.id = ?2", userId, albumId);
    }
}
