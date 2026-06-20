package dev.ecasept.unitodo.shared.utils;

import java.time.*;

public class DateFormat {
    public static long toLong(LocalDateTime time) {
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    public static LocalDateTime fromLong(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    public static long toLong(LocalDate date) {
        return date.toEpochDay();
    }

    public static LocalDate dateFromLong(long epochDay) {
        return LocalDate.ofEpochDay(epochDay);
    }

    public static long toLong(LocalTime time) {
        return time.toNanoOfDay();
    }

    public static LocalTime timeFromLong(long nanoOfDay) {
        return LocalTime.ofNanoOfDay(nanoOfDay);
    }


    public static String toString(LocalDateTime time) {
        return String.valueOf(toLong(time));
    }
    public static LocalDateTime fromString(String time) {
        return fromLong(Long.parseLong(time));
    }
}
