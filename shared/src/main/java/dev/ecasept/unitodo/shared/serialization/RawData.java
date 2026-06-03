package dev.ecasept.unitodo.shared.serialization;

import java.nio.charset.StandardCharsets;

public record RawData(byte[] data) {
    public static RawData fromString(String s) {
        return new RawData(s.getBytes(StandardCharsets.UTF_8));
    }
}
