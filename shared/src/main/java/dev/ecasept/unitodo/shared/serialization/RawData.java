package dev.ecasept.unitodo.shared.serialization;

import java.nio.charset.StandardCharsets;

/**
 * A class that stores a raw byte string. Features custom serialization logic that reads/writes the content directly from/to the buffer.
 * <p>
 * This is useful for cases where you want a piece of data to be passed through the serialization process without being modified.
 * This can happen when you are forced to pass some data through the serializer even though you want to send the data in a different format (not using the serializer's format).
 * @param data the raw byte data
 */
public record RawData(byte[] data) {
    public static RawData fromString(String s) {
        return new RawData(s.getBytes(StandardCharsets.UTF_8));
    }
    public String asString() {
        return new String(data, StandardCharsets.UTF_8);
    }
}
