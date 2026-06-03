package dev.ecasept.unitodo.models.serialization.serializers;

import dev.ecasept.unitodo.models.serialization.GrowableBuffer;
import dev.ecasept.unitodo.models.serialization.SerializationException;
import dev.ecasept.unitodo.utils.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ArraySerializer extends BaseSerializer {
    private static final String TAG = "ArraySerializer";

    private final DaddySerializer daddySerializer;

    public ArraySerializer(DaddySerializer daddySerializer) {
        this.daddySerializer = daddySerializer;
    }

    public void serialize(Object[] arr, GrowableBuffer buf, boolean[] nullableElements, int dimension) {
        Log.i(TAG, "Serializing array of length: " + arr.length);
        serializeLength(arr.length, buf);
        for (Object o : arr) {
            boolean nullableAt = dimension < nullableElements.length && nullableElements[dimension];
            daddySerializer.serialize(o, null, buf, nullableAt, nullableElements, dimension + 1);
        }
    }
    public void serializePrimitive(Object arr, GrowableBuffer buf) {
        Class<?> cmpType = arr.getClass().getComponentType();
        Log.i(TAG, "Serializing primitive array with and component type: " + cmpType.getName());
        if (cmpType == byte.class)    { serializeLength(((byte[])arr).length, buf);for (Object o : (byte[]) arr) buf.putByte((byte) o); return; }
        if (cmpType == int.class)     { serializeLength(((int[])arr).length, buf);for (Object o : (int[]) arr) buf.putInt((int) o); return; }
        if (cmpType == long.class)    { serializeLength(((long[])arr).length, buf);for (Object o : (long[]) arr) buf.putLong((long) o); return; }
        if (cmpType == short.class)   { serializeLength(((short[])arr).length, buf);for (Object o : (short[]) arr) buf.putShort((short) o); return; }
        if (cmpType == float.class)   { serializeLength(((float[])arr).length, buf);for (Object o : (float[]) arr) buf.putFloat((float) o); return; }
        if (cmpType == double.class)  { serializeLength(((double[])arr).length, buf);for (Object o : (double[]) arr) buf.putDouble((double) o); return; }
        if (cmpType == boolean.class) { serializeLength(((boolean[])arr).length, buf);for (Object o : (boolean[]) arr) buf.putByte(serializeBoolean((boolean) o)); return; }
        if (cmpType == char.class)    { serializeLength(((char[])arr).length, buf);for (Object o : (char[]) arr) buf.putChar((char) o); return; }
        throw new IllegalStateException("Unknown primitive type: " + cmpType.getName());
    }

    private <T> Object deserializePrimitiveArray(ByteBuffer data, Class<T> cmpType, int len) {
        Log.i(TAG, "Deserializing primitive array of length: " + len + " and component type: " + cmpType.getName());
        try {
            if (cmpType == byte.class)    { byte[]   arr = new byte[len];   for (int i = 0; i < len; i++) arr[i] = data.get();         return arr; }
            if (cmpType == int.class)     { int[]    arr = new int[len];    for (int i = 0; i < len; i++) arr[i] = data.getInt();      return arr; }
            if (cmpType == long.class)    { long[]   arr = new long[len];   for (int i = 0; i < len; i++) arr[i] = data.getLong();     return arr; }
            if (cmpType == short.class)   { short[]  arr = new short[len];  for (int i = 0; i < len; i++) arr[i] = data.getShort();    return arr; }
            if (cmpType == float.class)   { float[]  arr = new float[len];  for (int i = 0; i < len; i++) arr[i] = data.getFloat();    return arr; }
            if (cmpType == double.class)  { double[] arr = new double[len]; for (int i = 0; i < len; i++) arr[i] = data.getDouble();   return arr; }
            if (cmpType == boolean.class) { boolean[] arr = new boolean[len]; for (int i = 0; i < len; i++) arr[i] = deserializeBoolean(data.get()); return arr; }
            if (cmpType == char.class)    { char[]   arr = new char[len];   for (int i = 0; i < len; i++) arr[i] = data.getChar();     return arr; }
            throw new IllegalStateException("Unknown primitive type: " + cmpType.getName());
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Not enough data to read array of length " + len + " and component type " + cmpType.getName(), e);
        }
    }

    public <T> Object deserialize(ByteBuffer data, Class<T> clazz, boolean[] nullableElements, int dimension) {
        int len = deserializeLength(data);
        Class<?> cmpType = clazz.getComponentType();
        if (cmpType == null) {
            throw new IllegalArgumentException("Tried to deserialize array for non-array type " + clazz.getName());
        }
        if (len < 0) {
            throw new IllegalArgumentException("Tried to deserialize array with negative length " + len);
        }
        if (cmpType.isPrimitive()) {
            return deserializePrimitiveArray(data, cmpType, len);
        }
        Log.i(TAG, "Deserializing array of length: " + len);

        Object[] arr = (Object[]) java.lang.reflect.Array.newInstance(cmpType, len);
        for (int i = 0; i < len; i++) {
            boolean nullableAt = dimension < nullableElements.length && nullableElements[dimension];
            arr[i] = daddySerializer.deserialize(data, cmpType, nullableAt, nullableElements, dimension + 1);
        }
        return arr;
    }
}