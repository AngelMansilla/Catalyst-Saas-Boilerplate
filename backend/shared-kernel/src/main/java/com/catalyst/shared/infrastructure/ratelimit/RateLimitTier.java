package com.catalyst.shared.infrastructure.ratelimit;

/**
 * Enumeration of rate limiting tiers.
 */
public enum RateLimitTier {
    
    /**
     * Anonymous users (no authentication).
     */
    ANONYMOUS,
    
    /**
     * Authenticated users with free tier.
     */
    AUTHENTICATED,
    
    /**
     * Premium users with paid subscription.
     */
    PREMIUM,
    
    /**
     * Custom tier defined per endpoint.
     */
    CUSTOM
}

