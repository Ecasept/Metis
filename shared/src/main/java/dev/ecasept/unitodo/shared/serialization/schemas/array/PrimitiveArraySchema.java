package dev.ecasept.unitodo.shared.serialization.schemas.array;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.schemas.SerializationUtils;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public record PrimitiveArraySchema<T>(TypeContainer<T> type) implements Schema<T> {

    private static final String TAG = "PrimitiveArraySchema";

    @Override
    public void serialize(T o, GrowableBuffer buf) {
        Class<?> cmpType = type.asClass().getComponentType();
        Log.i(TAG, "Serializing primitive array with component type: " + cmpType.getName());

        if (cmpType == byte.class)    { byte[] arr = (byte[]) o;       SerializationUtils.serializeLength(arr.length, buf); for (byte b : arr) buf.putByte(b); return; }
        if (cmpType == int.class)     { int[] arr = (int[]) o;         SerializationUtils.serializeLength(arr.length, buf); for (int v : arr) buf.putInt(v); return; }
        if (cmpType == long.class)    { long[] arr = (long[]) o;       SerializationUtils.serializeLength(arr.length, buf); for (long v : arr) buf.putLong(v); return; }
        if (cmpType == short.class)   { short[] arr = (short[]) o;     SerializationUtils.serializeLength(arr.length, buf); for (short v : arr) buf.putShort(v); return; }
        if (cmpType == float.class)   { float[] arr = (float[]) o;     SerializationUtils.serializeLength(arr.length, buf); for (float v : arr) buf.putFloat(v); return; }
        if (cmpType == double.class)  { double[] arr = (double[]) o;   SerializationUtils.serializeLength(arr.length, buf); for (double v : arr) buf.putDouble(v); return; }
        if (cmpType == char.class)    { char[] arr = (char[]) o;       SerializationUtils.serializeLength(arr.length, buf); for (char v : arr) buf.putChar(v); return; }
        if (cmpType == boolean.class) { boolean[] arr = (boolean[]) o; SerializationUtils.serializeLength(arr.length, buf); for (boolean v : arr) buf.putByte(SerializationUtils.serializeBoolean(v)); return; }
        throw new IllegalStateException("Unknown primitive type: " + cmpType.getName());
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        Class<?> clazz = type.asClass();
        Class<?> cmpType = clazz.getComponentType();
        Log.i(TAG, "Deserializing primitive array with component type: " + cmpType.getName());

        int len;
        try {
            len = SerializationUtils.deserializeLength(data);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read length for primitive array of type " + clazz.getName(), e);
        }
        if (len < 0) {
            throw new SerializationException("Negative array length " + len + " found while deserializing primitive array of type " + clazz.getName());
        }

        try {
            Object arr;
            if (cmpType == byte.class)    { byte[] a = new byte[len];    for (int i = 0; i < len; i++) a[i] = data.get();                    arr = a; }
            else if (cmpType == int.class)     { int[] a = new int[len];      for (int i = 0; i < len; i++) a[i] = data.getInt();                 arr = a; }
            else if (cmpType == long.class)    { long[] a = new long[len];    for (int i = 0; i < len; i++) a[i] = data.getLong();                arr = a; }
            else if (cmpType == short.class)   { short[] a = new short[len];  for (int i = 0; i < len; i++) a[i] = data.getShort();               arr = a; }
            else if (cmpType == float.class)   { float[] a = new float[len];  for (int i = 0; i < len; i++) a[i] = data.getFloat();               arr = a; }
            else if (cmpType == double.class)  { double[] a = new double[len];for (int i = 0; i < len; i++) a[i] = data.getDouble();              arr = a; }
            else if (cmpType == char.class)    { char[] a = new char[len];    for (int i = 0; i < len; i++) a[i] = data.getChar();                arr = a; }
            else if (cmpType == boolean.class) { boolean[] a = new boolean[len]; for (int i = 0; i < len; i++) a[i] = SerializationUtils.deserializeBoolean(data.get()); arr = a; }
            else throw new IllegalStateException("Unknown primitive type: " + cmpType.getName());

            return type.cast(arr);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Not enough data to read primitive array of length " + len + " and component type " + cmpType.getName(), e);
        }
    }
}