package com.catalyst.shared.infrastructure.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Configuration properties for rate limiting.
 * 
 * <p>Configure in application.yml:
 * <pre>
 * catalyst:
 *   security:
 *     rate-limit:
 *       enabled: true
 *       anonymous:
 *         requests-per-minute: 30
 *         burst: 10
 *       authenticated:
 *         requests-per-minute: 100
 *         burst: 20
 *       premium:
 *         requests-per-minute: 500
 *         burst: 50
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "catalyst.security.rate-limit")
public class RateLimitProperties {
    
    /**
     * Whether rate limiting is enabled.
     */
    private boolean enabled = true;
    
    /**
     * Rate limits for anonymous (unauthenticated) users.
     */
    private TierConfig anonymous = new TierConfig(30, 10, Duration.ofMinutes(1));
    
    /**
     * Rate limits for authenticated users.
     */
    private TierConfig authenticated = new TierConfig(100, 20, Duration.ofMinutes(1));
    
    /**
     * Rate limits for premium users.
     */
    private TierConfig premium = new TierConfig(500, 50, Duration.ofMinutes(1));
    
    /**
     * Fallback to in-memory rate limiting if Redis is unavailable.
     */
    private boolean fallbackToMemory = true;
    
    /**
     * TTL for rate limit buckets in Redis.
     */
    private Duration bucketTtl = Duration.ofHours(1);
    
    // Getters and Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public TierConfig getAnonymous() {
        return anonymous;
    }
    
    public void setAnonymous(TierConfig anonymous) {
        this.anonymous = anonymous;
    }
    
    public TierConfig getAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(TierConfig authenticated) {
        this.authenticated = authenticated;
    }
    
    public TierConfig getPremium() {
        return premium;
    }
    
    public void setPremium(TierConfig premium) {
        this.premium = premium;
    }
    
    public boolean isFallbackToMemory() {
        return fallbackToMemory;
    }
    
    public void setFallbackToMemory(boolean fallbackToMemory) {
        this.fallbackToMemory = fallbackToMemory;
    }
    
    public Duration getBucketTtl() {
        return bucketTtl;
    }
    
    public void setBucketTtl(Duration bucketTtl) {
        this.bucketTtl = bucketTtl;
    }
    
    /**
     * Configuration for a rate limit tier.
     */
    public static class TierConfig {
        
        private int requestsPerMinute;
        private int burst;
        private Duration window;
        
        public TierConfig() {
            this(100, 20, Duration.ofMinutes(1));
        }
        
        public TierConfig(int requestsPerMinute, int burst, Duration window) {
            this.requestsPerMinute = requestsPerMinute;
            this.burst = burst;
            this.window = window;
        }
        
        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }
        
        public void setRequestsPerMinute(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
        
        public int getBurst() {
            return burst;
        }
        
        public void setBurst(int burst) {
            this.burst = burst;
        }
        
        public Duration getWindow() {
            return window;
        }
        
        public void setWindow(Duration window) {
            this.window = window;
        }
    }
}

