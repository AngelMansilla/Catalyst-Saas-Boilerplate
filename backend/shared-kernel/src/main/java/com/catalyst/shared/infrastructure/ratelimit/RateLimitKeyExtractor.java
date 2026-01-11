package com.catalyst.shared.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for extracting rate limit keys from requests.
 * The key is used to identify the rate limit bucket.
 */
@FunctionalInterface
public interface RateLimitKeyExtractor {
    
    /**
     * Extracts a unique key from the request.
     * The key should identify the client (user, IP, etc.).
     *
     * @param request the HTTP request
     * @return a unique key for rate limiting
     */
    String extractKey(HttpServletRequest request);
}

