package com.quarkus.security;

import com.quarkus.dto.response.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.Principal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class RateLimitFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final int LIMIT = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    @ConfigProperty(name = "app.rate-limit.enabled", defaultValue = "true")
    boolean rateLimitEnabled;
    private static final String RATE_LIMIT_REMAINING_PROPERTY = "rateLimitRemaining";

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(LIMIT, Refill.intervally(LIMIT, REFILL_PERIOD)))
                .build();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (!rateLimitEnabled) {
            return; // Skip rate limiting if disabled
        }

        Principal principal = requestContext.getSecurityContext().getUserPrincipal();
        if (principal == null) {
            return; // Skip rate limiting for unauthenticated requests
        }

        String userId = principal.getName();
        Bucket bucket = buckets.computeIfAbsent(userId, k -> createBucket());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long secondsToWait = probe.getNanosToWaitForRefill() / 1_000_000_000;
            requestContext.abortWith(Response.status(429)
                    .header("X-RateLimit-Limit", LIMIT)
                    .header("X-RateLimit-Remaining", 0)
                    .header("X-RateLimit-Reset", secondsToWait)
                    .entity(new ErrorResponse(
                            429,
                            "Too Many Requests",
                            requestContext.getUriInfo().getPath()
                    ))
                    .build());
        } else {
            // Store remaining tokens for response filter
            requestContext.setProperty(RATE_LIMIT_REMAINING_PROPERTY, probe.getRemainingTokens());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Long remaining = (Long) requestContext.getProperty(RATE_LIMIT_REMAINING_PROPERTY);
        if (remaining != null) {
            responseContext.getHeaders().add("X-RateLimit-Limit", LIMIT);
            responseContext.getHeaders().add("X-RateLimit-Remaining", remaining);
        }
    }
}
