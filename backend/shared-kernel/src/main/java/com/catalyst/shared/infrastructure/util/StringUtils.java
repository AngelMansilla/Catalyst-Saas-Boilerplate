package com.catalyst.shared.infrastructure.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for string operations.
 */
public final class StringUtils {
    
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_TAGS = Pattern.compile(
        "<script[^>]*>.*?</script>", 
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private StringUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Checks if a string is null or blank.
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
    
    /**
     * Checks if a string is not null and not blank.
     */
    public static boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }
    
    /**
     * Trims a string safely (returns null for null input).
     */
    public static String trimSafe(String str) {
        return str != null ? str.trim() : null;
    }
    
    /**
     * Trims a string, returning empty string for null input.
     */
    public static String trimToEmpty(String str) {
        return str != null ? str.trim() : "";
    }
    
    /**
     * Truncates a string to the specified length.
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
    
    /**
     * Truncates with ellipsis.
     */
    public static String truncateWithEllipsis(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Converts a string to a URL-safe slug.
     */
    public static String toSlug(String str) {
        if (str == null) {
            return null;
        }
        
        // Normalize unicode
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        
        // Remove non-ASCII characters
        normalized = normalized.replaceAll("[^\\p{ASCII}]", "");
        
        // Remove non-alphanumeric (except spaces and hyphens)
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll("");
        
        // Replace whitespace with hyphens
        normalized = WHITESPACE.matcher(normalized.trim()).replaceAll("-");
        
        // Convert to lowercase
        return normalized.toLowerCase(Locale.ROOT);
    }
    
    /**
     * Sanitizes HTML content by removing all tags.
     */
    public static String stripHtml(String str) {
        if (str == null) {
            return null;
        }
        // First remove script tags and their content
        String result = SCRIPT_TAGS.matcher(str).replaceAll("");
        // Then remove all other HTML tags
        return HTML_TAGS.matcher(result).replaceAll("");
    }
    
    /**
     * Escapes HTML special characters.
     */
    public static String escapeHtml(String str) {
        if (str == null) {
            return null;
        }
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
    
    /**
     * Masks an email address for privacy.
     * Example: john.doe@example.com -> j***e@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domain;
        }
        
        return localPart.charAt(0) + "***" + 
               localPart.charAt(localPart.length() - 1) + domain;
    }
    
    /**
     * Masks a credit card number.
     * Example: 4111111111111111 -> ****1111
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "****" + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * Converts camelCase to snake_case.
     */
    public static String toSnakeCase(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }
    
    /**
     * Converts snake_case to camelCase.
     */
    public static String toCamelCase(String str) {
        if (str == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : str.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
}

