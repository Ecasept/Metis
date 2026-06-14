package dev.ecasept.unitodo.shared.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateFormat {
    public static long toLong(LocalDateTime time) {
        return time.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    public static LocalDateTime fromLong(long epoch) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }
    public static String toString(LocalDateTime time) {
        return String.valueOf(toLong(time));
    }
    public static LocalDateTime fromString(String time) {
        return fromLong(Long.parseLong(time));
    }
}
