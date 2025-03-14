package com.roytemplates.springboot3_api.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.roytemplates.springboot3_api.config.RateLimitProperties;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Filter implementation for API rate limiting using token bucket algorithm.
 * Different rate limits are applied based on user authentication status and role.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitProperties properties;

    private final Cache<String, Bucket> cache;

    public RateLimitFilter() {
        // Initialize cache with 1 hour expiration after last access
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(100000)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        // Skip rate limiting for authentication endpoints
        // if (request.getRequestURI().startsWith("/v1/auth")) {
        //     filterChain.doFilter(request, response);
        //     return;
        // }

        // Get bucket for the current user/IP
        String key = getClientKey(request);
        Bucket bucket = cache.get(key, k -> createBucket());

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            response.addHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            response.addHeader("X-RateLimit-Limit", String.valueOf(getBucketCapacity()));
            
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            long waitForRefill = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("Retry-After", String.valueOf(waitForRefill));
            response.getWriter().write("Rate limit exceeded. Please try again later.");
        }
    }

    private Bucket createBucket() {
        int capacity = getBucketCapacity();
        Refill refill = Refill.intervally(capacity, Duration.ofMinutes(properties.getTimeWindow()));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientKey(HttpServletRequest request) {
        // Get authenticated user if available
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return "user:" + authentication.getName();
        }
        // Fall back to IP address for unauthenticated requests
        return "ip:" + request.getRemoteAddr();
    }

    private int getBucketCapacity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return properties.getUnauthenticatedLimit();
        }

        // Check user roles
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("SUPER_ADMIN"));
        
        return isAdmin ? properties.getAdminLimit() : properties.getAuthenticatedLimit();
    }
}