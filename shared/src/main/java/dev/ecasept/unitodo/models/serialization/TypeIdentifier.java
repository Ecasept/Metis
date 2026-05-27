package dev.ecasept.unitodo.models.serialization;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum TypeIdentifier {
    Byte (0x01, Byte.class),
    Short (0x02, Short.class),
    Int (0x03, Integer.class),
    Long (0x04, Long.class),
    Float (0x05, Float.class),
    Double (0x06, Double.class),
    Boolean (0x07, Boolean.class),
    Char (0x08, Character.class),
    String (0x09, String.class),
    Array (0x0A, null),
    Object (0x0B, Object.class),
    Null (0x0C, null),
    Custom (0x0D, null);

    private final byte id;
    private final Class<?> clazz;
    private static final Map<Byte, TypeIdentifier> BY_BYTE;
    static {
        Map<Byte, TypeIdentifier> m = new HashMap<>();
        for (TypeIdentifier t : values()) {
            if (m.putIfAbsent(t.id, t) != null) {
                throw new IllegalStateException("Duplicate TypeIdentifier id: " + t.asDebugString());
            }
        }
        BY_BYTE = Collections.unmodifiableMap(m);
    }

    TypeIdentifier(int id, Class<?> clazz) {
        this.id = (byte)id;
        this.clazz = clazz;
    }

    public byte asByte() {
        return id;
    }
    public String asByteString() {
        return java.lang.String.format("0x%02X", id);
    }
    public String asDebugString() {
        return name() + " (" + asByteString() + ")";
    }
    public static TypeIdentifier fromByte(byte b) {
        return BY_BYTE.get(b);
    }
}