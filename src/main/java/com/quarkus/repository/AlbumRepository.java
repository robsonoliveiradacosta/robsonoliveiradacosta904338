package com.quarkus.repository;

import com.quarkus.entity.Album;
import com.quarkus.entity.ArtistType;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AlbumRepository implements PanacheRepository<Album> {

    /**
     * Find albums with pagination, sorting and optional filter by artist type.
     * When artistType is specified, returns only albums that have at least one artist of that type.
     *
     * @param page Page parameters (index and size)
     * @param sort Sorting criteria
     * @param artistType Optional artist type filter (SINGER or BAND)
     * @return List of albums matching the criteria
     */
    public List<Album> findWithFilters(Page page, Sort sort, ArtistType artistType) {
        if (artistType == null) {
            return findAll(sort).page(page).list();
        }

        // Query with JOIN to filter by artist type
        String query = "SELECT DISTINCT a FROM Album a JOIN a.artists artist WHERE artist.type = :artistType";

        Map<String, Object> params = new HashMap<>();
        params.put("artistType", artistType);

        return find(query, sort, params).page(page).list();
    }

    /**
     * Count albums with optional filter by artist type.
     *
     * @param artistType Optional artist type filter
     * @return Total number of albums matching the criteria
     */
    public long countWithFilters(ArtistType artistType) {
        if (artistType == null) {
            return count();
        }

        String query = "SELECT COUNT(DISTINCT a) FROM Album a JOIN a.artists artist WHERE artist.type = :artistType";
        return find(query, Map.of("artistType", artistType)).count();
    }
}
