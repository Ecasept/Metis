package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;

import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Provides helper methods for schemas
 */
public final class SerializationUtils {
    /** Serializes a value at the current position that indicates the length of some other data */
    public static void serializeLength(int length, GrowableBuffer buf) {
        buf.putInt(length);
    }
    /** Serializes a value at the specified position that indicates the length of some other data */
    public static void serializeLength(int length, int pos, GrowableBuffer buf) {
        buf.putInt(pos, length);
    }
    /** Deserializes a value at the current position that indicates the length of some other data */
    public static int deserializeLength(ByteBuffer buf) throws SerializationException {
        try {
            return buf.getInt();
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read length", e);
        }
    }

    /** Instantiates a new object of the specified class using its no-arg constructor.
     * @param clazz The class to instantiate
     * @param <T> The type of the class
     * @return A new instance of the specified class
     * @throws IllegalArgumentException If the class cannot be instantiated
     * @throws IllegalStateException If the class has no no-arg constructor
     */
    public static <T> T instantiateSerializableObject(Class<T> clazz) {
        T o;
        try {
            var constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            o = constructor.newInstance();
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalArgumentException("Failed to instantiate class " + clazz.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Class " + clazz.getName() + " has no no-arg constructor", e);
        }
        return o;
    }

    /** Deserializes a byte into a boolean value
     * @param b The byte that represents the boolean value
     * @return The byte interpreted as a byte
     * @throws SerializationException If the byte does not represent a valid boolean value
     */
    public static boolean deserializeBoolean(byte b) throws SerializationException {
        if (b != 0 && b != (byte) 0xFF) {
            throw new SerializationException("Invalid byte value for boolean: " + String.format("0x%02X", b));
        }
        return b != 0;
    }

    /** Serializes a boolean value into a byte
     * @param value The boolean value to serialize
     * @return The byte representation of the boolean value
     */
    public static byte serializeBoolean(boolean value) {
        return (byte) (value ? 0xFF : 0x00);
    }
}