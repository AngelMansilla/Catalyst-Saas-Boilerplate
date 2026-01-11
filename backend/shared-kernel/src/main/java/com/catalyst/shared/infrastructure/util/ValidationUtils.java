package com.catalyst.shared.infrastructure.util;

import com.catalyst.shared.domain.exception.ValidationException;

import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 */
public final class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );
    
    private ValidationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates that a value is not null.
     */
    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw ValidationException.required(fieldName);
        }
        return value;
    }
    
    /**
     * Validates that a string is not blank.
     */
    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw ValidationException.required(fieldName);
        }
        return value;
    }
    
    /**
     * Validates that a collection is not empty.
     */
    public static <T extends Collection<?>> T requireNotEmpty(T collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw ValidationException.required(fieldName);
        }
        return collection;
    }
    
    /**
     * Validates string length.
     */
    public static String requireLength(String value, int min, int max, String fieldName) {
        if (value == null) {
            throw ValidationException.required(fieldName);
        }
        if (value.length() < min) {
            throw ValidationException.tooShort(fieldName, min);
        }
        if (value.length() > max) {
            throw ValidationException.tooLong(fieldName, max);
        }
        return value;
    }
    
    /**
     * Validates email format.
     */
    public static String requireValidEmail(String email, String fieldName) {
        requireNotBlank(email, fieldName);
        if (!isValidEmail(email)) {
            throw ValidationException.invalidEmail(email);
        }
        return email.trim().toLowerCase();
    }
    
    /**
     * Checks if an email is valid.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validates UUID format.
     */
    public static UUID requireValidUuid(String uuid, String fieldName) {
        requireNotBlank(uuid, fieldName);
        if (!isValidUuid(uuid)) {
            throw ValidationException.invalidFormat(fieldName, "UUID");
        }
        return UUID.fromString(uuid);
    }
    
    /**
     * Checks if a string is a valid UUID.
     */
    public static boolean isValidUuid(String uuid) {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches();
    }
    
    /**
     * Validates phone number format (E.164).
     */
    public static String requireValidPhone(String phone, String fieldName) {
        requireNotBlank(phone, fieldName);
        if (!isValidPhone(phone)) {
            throw ValidationException.invalidFormat(fieldName, "E.164 phone number");
        }
        return phone;
    }
    
    /**
     * Checks if a phone number is valid (E.164 format).
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validates a positive number.
     */
    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(
                "VALIDATION.FIELD.MUST_BE_POSITIVE",
                String.format("%s must be positive", fieldName)
            );
        }
        return value;
    }
    
    /**
     * Validates a non-negative number.
     */
    public static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(
                "VALIDATION.FIELD.MUST_BE_NON_NEGATIVE",
                String.format("%s must be non-negative", fieldName)
            );
        }
        return value;
    }
    
    /**
     * Validates a number is within range.
     */
    public static int requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(
                "VALIDATION.FIELD.OUT_OF_RANGE",
                String.format("%s must be between %d and %d", fieldName, min, max)
            );
        }
        return value;
    }
}

