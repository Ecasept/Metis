package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;

/** A Schema represents the instructions for how to serialize a specific type */
public interface Schema<T> {
    /** Writes a byte representation of {@code o} into {@code buf} */
    void serialize(T o, GrowableBuffer buf);
    /** Tries to interpret the byte sequence lying in {@code data} as an object fitting the type {@link T} */
    T deserialize(ByteBuffer data) throws SerializationException;

    /**
     * Handles serialization of nullable types
     * <p>
     * This method can be called for any object.
     * If the object is {@code null}, this method will encode the marker for {@code null} values and return {@code true}.
     * Otherwise, it will encode the marker for non-null values and return {@code false}.
     * @param o The object to check
     * @param buf The buffer where the marker should be encoded
     * @return Whether the object was {@code null}
     */
    default boolean serializeNullable(T o, GrowableBuffer buf) {
        if (o == null) {
            Log.i("Schema", "Serializing null value");
            buf.putByte((byte) 0);
            return true;
        } else {
            buf.putByte((byte) 1);
            return false;
        }
    }

    /**
     * Handle deserialization of nullable types
     * <p>
     * This method should be called if a nullable type is expected.
     * It looks at the buffer and consumes the {@code null}/non-null marker.
     * @param data The buffer containing the serialized binary data
     * @return Whether a {@code null} marker was present or not, indicating the object that should have been encoded in the following space is {@code null}
     * @throws SerializationException If no valid {@code null} or non-null marker was found
     */
    default boolean deserializeNullable(ByteBuffer data) throws SerializationException {
        byte nullByte = data.get();
        if (nullByte == (byte) 0x00) {
            Log.i("Schema", "Deserialized null value");
            return true;
        } else if (nullByte != (byte) 0xFF) {
            throw new SerializationException("Invalid null byte value: " + String.format("0x%02X", nullByte));
        }
        return false;
    }
}
