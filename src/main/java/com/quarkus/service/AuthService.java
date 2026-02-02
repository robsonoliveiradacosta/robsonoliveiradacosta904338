package com.quarkus.service;

import com.quarkus.dto.request.LoginRequest;
import com.quarkus.dto.response.TokenResponse;
import com.quarkus.entity.User;
import com.quarkus.repository.UserRepository;
import com.quarkus.security.TokenService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    TokenService tokenService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        LOG.infof("Attempting login for user: %s", request.username());

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    LOG.warnf("User not found: %s", request.username());
                    return new NotAuthorizedException("Invalid credentials");
                });

        LOG.infof("User found: %s, verifying password", user.getUsername());

        boolean passwordMatches = BcryptUtil.matches(request.password(), user.getPasswordHash());

        if (!passwordMatches) {
            LOG.warnf("Password does not match for user: %s", request.username());
            throw new NotAuthorizedException("Invalid credentials");
        }

        LOG.infof("Password verified successfully for user: %s", user.getUsername());

        String token = tokenService.generateToken(user);
        long expiresIn = tokenService.getTokenLifespanInSeconds();

        return new TokenResponse(token, expiresIn);
    }

    public TokenResponse refresh(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotAuthorizedException("Invalid user"));

        String token = tokenService.generateToken(user);
        long expiresIn = tokenService.getTokenLifespanInSeconds();

        return new TokenResponse(token, expiresIn);
    }
}
