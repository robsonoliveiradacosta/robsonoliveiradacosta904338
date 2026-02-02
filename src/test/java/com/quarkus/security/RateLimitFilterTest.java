package com.quarkus.security;

import com.quarkus.dto.response.ErrorResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;
    private SecurityContext securityContext;
    private Principal principal;
    private UriInfo uriInfo;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        rateLimitFilter.rateLimitEnabled = true; // Enable rate limiting for unit tests
        requestContext = mock(ContainerRequestContext.class);
        responseContext = mock(ContainerResponseContext.class);
        securityContext = mock(SecurityContext.class);
        principal = mock(Principal.class);
        uriInfo = mock(UriInfo.class);

        when(requestContext.getSecurityContext()).thenReturn(securityContext);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("/api/v1/test");
    }

    @Test
    void shouldAllowRequestsWhenUnderLimit() {
        // Given
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("testuser");

        // When
        rateLimitFilter.filter(requestContext);

        // Then
        verify(requestContext, never()).abortWith(any(Response.class));
        verify(requestContext).setProperty(eq("rateLimitRemaining"), anyLong());
    }

    @Test
    void shouldBlockRequestsWhenLimitExceeded() {
        // Given
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("testuser");

        // When - make 10 requests (the limit)
        for (int i = 0; i < 10; i++) {
            rateLimitFilter.filter(requestContext);
        }

        // Then - 11th request should be blocked
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        rateLimitFilter.filter(requestContext);
        verify(requestContext, atLeastOnce()).abortWith(responseCaptor.capture());

        Response capturedResponse = responseCaptor.getValue();
        assertEquals(429, capturedResponse.getStatus());
        assertEquals("0", capturedResponse.getHeaderString("X-RateLimit-Remaining"));
        assertEquals("10", capturedResponse.getHeaderString("X-RateLimit-Limit"));
        assertNotNull(capturedResponse.getHeaderString("X-RateLimit-Reset"));

        ErrorResponse errorResponse = (ErrorResponse) capturedResponse.getEntity();
        assertEquals(429, errorResponse.status());
        assertEquals("Too Many Requests", errorResponse.message());
    }

    @Test
    void shouldSkipRateLimitingForUnauthenticatedRequests() {
        // Given
        when(securityContext.getUserPrincipal()).thenReturn(null);

        // When
        rateLimitFilter.filter(requestContext);

        // Then
        verify(requestContext, never()).abortWith(any(Response.class));
        verify(requestContext, never()).setProperty(anyString(), any());
    }

    @Test
    void shouldIsolateBucketsPerUser() {
        // Given
        Principal user1 = mock(Principal.class);
        Principal user2 = mock(Principal.class);
        when(user1.getName()).thenReturn("user1");
        when(user2.getName()).thenReturn("user2");

        // When - user1 makes 10 requests
        when(securityContext.getUserPrincipal()).thenReturn(user1);
        for (int i = 0; i < 10; i++) {
            rateLimitFilter.filter(requestContext);
        }

        // Then - user2 should still be able to make requests
        when(securityContext.getUserPrincipal()).thenReturn(user2);
        rateLimitFilter.filter(requestContext);
        verify(requestContext, never()).abortWith(any(Response.class));
    }

    @Test
    void shouldAddRateLimitHeadersToResponse() {
        // Given
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("testuser");
        when(requestContext.getProperty("rateLimitRemaining")).thenReturn(9L);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);

        // When
        rateLimitFilter.filter(requestContext);
        rateLimitFilter.filter(requestContext, responseContext);

        // Then
        assertEquals(10, headers.getFirst("X-RateLimit-Limit"));
        assertEquals(9L, headers.getFirst("X-RateLimit-Remaining"));
    }

    @Test
    void shouldNotAddHeadersWhenNoRemainingTokensProperty() {
        // Given
        when(requestContext.getProperty("rateLimitRemaining")).thenReturn(null);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);

        // When
        rateLimitFilter.filter(requestContext, responseContext);

        // Then
        assertFalse(headers.containsKey("X-RateLimit-Limit"));
        assertFalse(headers.containsKey("X-RateLimit-Remaining"));
    }
}
