package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/** The instructions for (de-)serializing a primitive type */
public record PrimitiveSchema<T>(Class<T> clazz) implements Schema<T> {
    private static final String TAG = "PrimitiveSerializer";

    @Override
    public void serialize(T o, GrowableBuffer buf) {
        switch (o) {
            case Byte b -> {
                Log.i(TAG, "Serializing byte: " + b);
                buf.putByte(b);
            }
            case Short s -> {
                Log.i(TAG, "Serializing short: " + s);
                buf.putShort(s);
            }
            case Integer i -> {
                Log.i(TAG, "Serializing int: " + i);
                buf.putInt(i);
            }
            case Long l -> {
                Log.i(TAG, "Serializing long: " + l);
                buf.putLong(l);
            }
            case Float f -> {
                Log.i(TAG, "Serializing float: " + f);
                buf.putFloat(f);
            }
            case Double d -> {
                Log.i(TAG, "Serializing double: " + d);
                buf.putDouble(d);
            }
            case Boolean b -> {
                Log.i(TAG, "Serializing boolean: " + b);
                buf.putByte(SerializationUtils.serializeBoolean(b));
            }
            case Character c -> {
                Log.i(TAG, "Serializing char: " + c);
                buf.putChar(c);
            }

            default -> throw new IllegalStateException("Primitive Serializer called for non-primitive type " + o.getClass().getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked") // Java can't understand that e.g., the cast (T) Integer.valueOf(data.getInt()) only happens when clazz is Integer.class and T therefore is Integer
    public T deserialize(ByteBuffer data) throws SerializationException {
        Log.i(TAG, "Deserializing primitive of type: " + clazz.getName());
        try {
            if (clazz == byte.class || clazz == Byte.class) {
                byte value = data.get();
                Log.i(TAG, "Deserialized byte: " + value);
                return (T) Byte.valueOf(value);
            }

            if (clazz == short.class || clazz == Short.class) {
                short value = data.getShort();
                Log.i(TAG, "Deserialized short: " + value);
                return (T) Short.valueOf(value);
            }

            if (clazz == int.class || clazz == Integer.class) {
                int value = data.getInt();
                Log.i(TAG, "Deserialized int: " + value);
                return (T) Integer.valueOf(value);
            }

            if (clazz == long.class || clazz == Long.class) {
                long value = data.getLong();
                Log.i(TAG, "Deserialized long: " + value);
                return (T) Long.valueOf(value);
            }

            if (clazz == float.class || clazz == Float.class) {
                float value = data.getFloat();
                Log.i(TAG, "Deserialized float: " + value);
                return (T) Float.valueOf(value);
            }

            if (clazz == double.class || clazz == Double.class) {
                double value = data.getDouble();
                Log.i(TAG, "Deserialized double: " + value);
                return (T) Double.valueOf(value);
            }

            if (clazz == boolean.class || clazz == Boolean.class) {
                byte boolByte = data.get();
                boolean value = SerializationUtils.deserializeBoolean(boolByte);
                Log.i(TAG, "Deserialized boolean: " + value);
                return (T) Boolean.valueOf(value);
            }

            if (clazz == char.class || clazz == Character.class) {
                char value = data.getChar();
                Log.i(TAG, "Deserialized char: " + value);
                return (T) Character.valueOf(value);
            }

            throw new IllegalStateException("Primitive Serializer called for non-primitive type " + clazz.getName());
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read value of type " + clazz.getName(), e);
        }
    }
}