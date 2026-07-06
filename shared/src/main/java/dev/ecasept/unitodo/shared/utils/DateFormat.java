package dev.ecasept.unitodo.shared.utils;

import java.time.*;

/**
 * Utility classes to format date.
 * Contains classes to serialize and deserialize LocalDateTime, LocalDate, and LocalTime to and from long and String values.
 */
public class DateFormat {
    /** Converts a LocalDateTime to a unix epoch value */
    public static long toLong(LocalDateTime time) {
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    /** Converts a unix epoch value to a LocalDateTime */
    public static LocalDateTime fromLong(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    /** Converts a LocalDate to a unix epoch value */
    public static long toLong(LocalDate date) {
        return date.toEpochDay();
    }

    /** Converts a unix epoch value to a LocalDate */
    public static LocalDate dateFromLong(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    /** Converts a LocalTime to a unix epoch value */
    public static long toLong(LocalTime time) {
        return time.toNanoOfDay();
    }

    /** Converts a unix epoch value to a LocalTime */
    public static LocalTime timeFromLong(long nanoOfDay) {
        return LocalTime.ofNanoOfDay(nanoOfDay);
    }

    /** Converts a LocalDateTime to a string representation of its unix epoch value */
    public static String toString(LocalDateTime time) {
        return String.valueOf(toLong(time));
    }

    /** Converts a string representation of a unix epoch value to a LocalDateTime */
    public static LocalDateTime fromString(String time) {
        return fromLong(Long.parseLong(time));
    }
}
