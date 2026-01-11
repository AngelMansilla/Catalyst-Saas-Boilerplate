package com.catalyst.shared.infrastructure.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Utility class for date and time operations.
 */
public final class DateTimeUtils {
    
    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_INSTANT;
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private DateTimeUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Gets the current instant in UTC.
     */
    public static Instant now() {
        return Instant.now();
    }
    
    /**
     * Gets the current date in UTC.
     */
    public static LocalDate today() {
        return LocalDate.now(UTC);
    }
    
    /**
     * Gets the current date-time in UTC.
     */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(UTC);
    }
    
    /**
     * Converts an Instant to a formatted ISO string.
     */
    public static String formatIso(Instant instant) {
        return instant != null ? ISO_DATE_TIME.format(instant) : null;
    }
    
    /**
     * Parses an ISO date-time string to an Instant.
     */
    public static Optional<Instant> parseIso(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(dateTimeString));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Checks if an instant is in the past.
     */
    public static boolean isPast(Instant instant) {
        return instant != null && instant.isBefore(Instant.now());
    }
    
    /**
     * Checks if an instant is in the future.
     */
    public static boolean isFuture(Instant instant) {
        return instant != null && instant.isAfter(Instant.now());
    }
    
    /**
     * Calculates the duration between two instants.
     */
    public static Duration between(Instant start, Instant end) {
        return Duration.between(start, end);
    }
    
    /**
     * Adds a duration to an instant.
     */
    public static Instant plus(Instant instant, Duration duration) {
        return instant.plus(duration);
    }
    
    /**
     * Gets the start of the day in UTC.
     */
    public static Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(UTC).toInstant();
    }
    
    /**
     * Gets the end of the day in UTC.
     */
    public static Instant endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX).atZone(UTC).toInstant();
    }
    
    /**
     * Converts epoch milliseconds to Instant.
     */
    public static Instant fromEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }
    
    /**
     * Converts Instant to epoch milliseconds.
     */
    public static long toEpochMilli(Instant instant) {
        return instant.toEpochMilli();
    }
    
    /**
     * Converts epoch seconds to Instant.
     */
    public static Instant fromEpochSecond(long epochSecond) {
        return Instant.ofEpochSecond(epochSecond);
    }
    
    /**
     * Converts Instant to epoch seconds.
     */
    public static long toEpochSecond(Instant instant) {
        return instant.getEpochSecond();
    }
}

