package com.catalyst.shared.infrastructure.ratelimit;

import com.catalyst.shared.domain.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for rate limiting using Bucket4j.
 * Uses in-memory buckets with fallback capability.
 */
@Service
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitService {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    
    private final RateLimitProperties properties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Checks if the request is allowed based on rate limits.
     *
     * @param key the rate limit key (user ID or IP)
     * @param tier the rate limit tier
     * @return rate limit result
     */
    public RateLimitResult tryConsume(String key, RateLimitTier tier) {
        if (!properties.isEnabled()) {
            return RateLimitResult.success();
        }
        
        Bucket bucket = getBucket(key, tier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            return RateLimitResult.success(
                getLimit(tier),
                (int) probe.getRemainingTokens(),
                probe.getNanosToWaitForReset() / 1_000_000_000
            );
        }
        
        long retryAfter = probe.getNanosToWaitForRefill() / 1_000_000_000;
        log.debug("Rate limit exceeded for key: {}, retry after: {}s", key, retryAfter);
        
        return RateLimitResult.denied(
            getLimit(tier),
            0,
            retryAfter
        );
    }
    
    /**
     * Checks if the request is allowed, throws exception if not.
     */
    public RateLimitResult consumeOrThrow(String key, RateLimitTier tier) {
        RateLimitResult result = tryConsume(key, tier);
        
        if (!result.isAllowed()) {
            throw new RateLimitExceededException(
                "Too many requests. Please try again later.",
                result.retryAfterSeconds(),
                result.limit(),
                result.remaining()
            );
        }
        
        return result;
    }
    
    /**
     * Gets or creates a bucket for the given key and tier.
     */
    private Bucket getBucket(String key, RateLimitTier tier) {
        String bucketKey = tier.name() + ":" + key;
        return buckets.computeIfAbsent(bucketKey, k -> createBucket(tier));
    }
    
    /**
     * Creates a new bucket for the specified tier.
     */
    private Bucket createBucket(RateLimitTier tier) {
        RateLimitProperties.TierConfig config = getTierConfig(tier);
        
        Bandwidth limit = Bandwidth.builder()
            .capacity(config.getBurst())
            .refillGreedy(config.getRequestsPerMinute(), config.getWindow())
            .build();
        
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    /**
     * Gets the configuration for a tier.
     */
    private RateLimitProperties.TierConfig getTierConfig(RateLimitTier tier) {
        return switch (tier) {
            case ANONYMOUS -> properties.getAnonymous();
            case AUTHENTICATED -> properties.getAuthenticated();
            case PREMIUM -> properties.getPremium();
            case CUSTOM -> properties.getAuthenticated(); // Default to authenticated for custom
        };
    }
    
    /**
     * Gets the limit for a tier.
     */
    private int getLimit(RateLimitTier tier) {
        return getTierConfig(tier).getRequestsPerMinute();
    }
    
    /**
     * Clears all cached buckets (for testing).
     */
    public void clearBuckets() {
        buckets.clear();
    }
    
    /**
     * Result of a rate limit check.
     */
    public record RateLimitResult(
        boolean allowed,
        int limit,
        int remaining,
        long retryAfterSeconds
    ) {
        
        public static RateLimitResult success() {
            return new RateLimitResult(true, 0, 0, 0);
        }
        
        public static RateLimitResult success(int limit, int remaining, long resetSeconds) {
            return new RateLimitResult(true, limit, remaining, resetSeconds);
        }
        
        public static RateLimitResult denied(int limit, int remaining, long retryAfterSeconds) {
            return new RateLimitResult(false, limit, remaining, retryAfterSeconds);
        }
        
        public boolean isAllowed() {
            return allowed;
        }
    }
}

