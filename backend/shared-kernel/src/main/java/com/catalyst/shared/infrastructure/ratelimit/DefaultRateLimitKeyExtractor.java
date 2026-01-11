package com.catalyst.shared.infrastructure.ratelimit;

import com.catalyst.shared.infrastructure.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Default implementation of RateLimitKeyExtractor.
 * Uses user ID for authenticated requests, IP address for anonymous.
 */
@Component
public class DefaultRateLimitKeyExtractor implements RateLimitKeyExtractor {
    
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    
    @Override
    public String extractKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal user) {
            return "user:" + user.id().toString();
        }
        
        return "ip:" + getClientIp(request);
    }
    
    /**
     * Extracts the client IP address, considering proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }
}

