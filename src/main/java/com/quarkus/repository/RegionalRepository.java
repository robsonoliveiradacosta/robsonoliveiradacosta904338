package com.quarkus.repository;

import com.quarkus.entity.Regional;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RegionalRepository implements PanacheRepositoryBase<Regional, Integer> {

    public List<Regional> findAllActive() {
        return list("active", true);
    }
}
