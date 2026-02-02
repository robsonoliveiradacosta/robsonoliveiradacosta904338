package com.quarkus.common;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {

    private final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18.1-alpine3.23");

    @Override
    public Map<String, String> start() {
        postgres.withUsername("user_test")
                .withPassword("password_test")
                .withDatabaseName("music_catalog_test")
                .start();

        return Map.of(
                "quarkus.datasource.jdbc.url", postgres.getJdbcUrl(),
                "quarkus.datasource.username", postgres.getUsername(),
                "quarkus.datasource.password", postgres.getPassword()
        );
    }

    @Override
    public void stop() {
        postgres.stop();
    }
}
