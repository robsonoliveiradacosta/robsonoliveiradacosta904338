package com.quarkus.repository;

import com.quarkus.entity.AlbumImage;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AlbumImageRepository implements PanacheRepository<AlbumImage> {

    /**
     * Find an image by album ID and hash.
     *
     * @param albumId Album ID
     * @param hash Image hash
     * @return Optional containing the AlbumImage if found
     */
    public Optional<AlbumImage> findByAlbumIdAndHash(Long albumId, String hash) {
        return find("album.id = ?1 and hash = ?2", albumId, hash).firstResultOptional();
    }

    /**
     * Find all images for a specific album.
     *
     * @param albumId Album ID
     * @return List of AlbumImage
     */
    public List<AlbumImage> findByAlbumId(Long albumId) {
        return find("album.id", albumId).list();
    }

    /**
     * Delete all images for a specific album.
     *
     * @param albumId Album ID
     * @return Number of deleted images
     */
    public long deleteByAlbumId(Long albumId) {
        return delete("album.id", albumId);
    }
}
