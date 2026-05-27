package dev.ecasept.unitodo.models.serialization.serializers;

import dev.ecasept.unitodo.models.serialization.GrowableBuffer;
import dev.ecasept.unitodo.models.serialization.SerializationException;
import dev.ecasept.unitodo.models.serialization.TypeIdentifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Provides helper methods for other Serializers
 */
public abstract class BaseSerializer {

    /** Checks if the given class is a wrapper type for a primitive (e.g. Integer for int) */
    protected static boolean isWrapper(Class<?> clazz) {
        return clazz.equals(Byte.class)
                || clazz.equals(Short.class)
                || clazz.equals(Integer.class)
                || clazz.equals(Long.class)
                || clazz.equals(Float.class)
                || clazz.equals(Double.class)
                || clazz.equals(Boolean.class)
                || clazz.equals(Character.class);
    }

    /** Serializes a value at the current position that indicates the length of some other data */
    protected void serializeLength(int length, GrowableBuffer buf) {
        buf.putInt(length);
    }
    /** Serializes a value at the specified position that indicates the length of some other data */
    protected void serializeLength(int length, int pos, GrowableBuffer buf) {
        buf.putInt(pos, length);
    }
    /** Deserializes a value at the current position that indicates the length of some other data */
    protected int deserializeLength(ByteBuffer buf) {
        try {
            return buf.getInt();
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read length", e);
        }
    }

    protected <T> T instatiateSerializableObject(Class<T> clazz) {
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

    protected boolean deserializeBoolean(byte b) {
        if (b != 0 && b != (byte) 0xFF) {
            throw new SerializationException("Invalid byte value for boolean: " + String.format("0x%02X", b));
        }
        return b != 0;
    }

    protected byte serializeBoolean(boolean value) {
        return (byte) (value ? 0xFF : 0x00);
    }
}