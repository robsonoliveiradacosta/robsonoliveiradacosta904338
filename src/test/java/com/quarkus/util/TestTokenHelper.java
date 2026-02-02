package com.quarkus.util;

import io.smallrye.jwt.build.Jwt;

import java.time.Duration;
import java.util.Set;

public class TestTokenHelper {

    public static String generateAdminToken() {
        return Jwt.issuer("quarkus-api")
                .upn("admin")
                .groups(Set.of("ADMIN"))
                .expiresIn(Duration.ofMinutes(5))
                .sign();
    }

    public static String generateUserToken() {
        return Jwt.issuer("quarkus-api")
                .upn("user")
                .groups(Set.of("USER"))
                .expiresIn(Duration.ofMinutes(5))
                .sign();
    }
}
