package com.quarkus.repository;

import com.quarkus.entity.Artist;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ArtistRepository implements PanacheRepository<Artist> {

    /**
     * Find artists by name containing the search term (case-insensitive) with optional sorting.
     *
     * @param name Optional name filter (partial match, case-insensitive)
     * @param sort Sorting criteria
     * @return List of matching artists
     */
    public List<Artist> findByNameContaining(String name, Sort sort) {
        if (name == null || name.isBlank()) {
            return listAll(sort);
        }
        return list("LOWER(name) LIKE LOWER(?1)", sort, "%" + name + "%");
    }
}
