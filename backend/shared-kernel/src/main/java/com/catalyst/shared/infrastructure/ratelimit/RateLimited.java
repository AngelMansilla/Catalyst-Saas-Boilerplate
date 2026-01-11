package com.catalyst.shared.infrastructure.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Annotation to apply rate limiting to controller methods or classes.
 * 
 * <p>Usage examples:
 * <pre>
 * // Use tier-based configuration
 * &#64;RateLimited(tier = RateLimitTier.AUTHENTICATED)
 * 
 * // Custom rate limit
 * &#64;RateLimited(limit = 10, duration = 1, unit = ChronoUnit.MINUTES)
 * 
 * // Custom rate limit for login endpoint
 * &#64;RateLimited(limit = 5, duration = 1, unit = ChronoUnit.MINUTES, 
 *              keyExtractor = IpKeyExtractor.class)
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    
    /**
     * The rate limit tier to use.
     * If CUSTOM, the limit and duration values will be used.
     */
    RateLimitTier tier() default RateLimitTier.AUTHENTICATED;
    
    /**
     * Maximum number of requests allowed in the time window.
     * Only used when tier is CUSTOM.
     */
    int limit() default 100;
    
    /**
     * Duration of the time window.
     * Only used when tier is CUSTOM.
     */
    int duration() default 1;
    
    /**
     * Time unit for the duration.
     * Only used when tier is CUSTOM.
     */
    ChronoUnit unit() default ChronoUnit.MINUTES;
    
    /**
     * Burst capacity (initial tokens).
     * Only used when tier is CUSTOM.
     */
    int burst() default 20;
    
    /**
     * The key extractor class to use.
     * Default extracts from authenticated user or IP address.
     */
    Class<? extends RateLimitKeyExtractor> keyExtractor() default DefaultRateLimitKeyExtractor.class;
    
    /**
     * Message to return when rate limit is exceeded.
     */
    String message() default "Too many requests. Please try again later.";
}

