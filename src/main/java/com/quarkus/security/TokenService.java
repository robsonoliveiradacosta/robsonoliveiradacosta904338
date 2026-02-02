package com.quarkus.security;

import com.quarkus.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    private static final String ISSUER = "quarkus-api";
    private static final Duration TOKEN_LIFESPAN = Duration.ofMinutes(5);

    public String generateToken(User user) {
        return Jwt.issuer(ISSUER)
                .upn(user.getUsername())
                .groups(Set.of(user.getRole().name()))
                .expiresIn(TOKEN_LIFESPAN)
                .sign();
    }

    public long getTokenLifespanInSeconds() {
        return TOKEN_LIFESPAN.getSeconds();
    }
}
