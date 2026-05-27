package dev.ecasept.unitodo.models.serialization.serializers;

import dev.ecasept.unitodo.models.serialization.GrowableBuffer;
import dev.ecasept.unitodo.models.serialization.SerializationException;
import dev.ecasept.unitodo.models.serialization.adapters.Adapter;
import dev.ecasept.unitodo.utils.Log;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DaddySerializer extends BaseSerializer {
    public static final String TAG = "DaddySerializer";

    private final ArraySerializer arraySerializer = new ArraySerializer(this);
    private final PrimitiveSerializer primitiveSerializer = new PrimitiveSerializer();
    private final WrapperSerializer wrapperSerializer = new WrapperSerializer();
    private final StringSerializer stringSerializer = new StringSerializer();
    private final ObjectSerializer objectSerializer = new ObjectSerializer(this);
    private final HashMap<Class<?>, Adapter<?>> adapters;

    public DaddySerializer(ArrayList<Adapter<?>> adapters) {
        this.adapters = adapters;
        adapters.forEach(a -> a.getClass().getComponentType());
    }

    public void serialize(Object o, Class<?> clazz, GrowableBuffer buf, boolean nullable, boolean[] nullableElements) {
        serialize(o, clazz, buf, nullable, nullableElements, 0);
    }

    public void serialize(Object o, Class<?> clazz, GrowableBuffer buf, boolean nullable, boolean[] nullableElements, int arrDim) {
        if (o == null) {
            if (!nullable) {
                throw new IllegalArgumentException("Tried to serialize null value for non-nullable type");
            }
            Log.i(TAG, "Serializing null value");
            buf.putByte((byte)0x00);
            return;
        }
        if (clazz == null) {
            // This means that the caller didn't know the class of the object to serialize, i.e. when it can't be certain that the object isn't null.
            clazz = o.getClass();
        }
        Log.i(TAG, "Serializing object of class: " + clazz.getName());
        if (clazz.isPrimitive()) {
            primitiveSerializer.serialize(o, buf);
        } else if (isWrapper(clazz)) {
            buf.putByte((byte)0xFF);
            wrapperSerializer.serialize(o, buf);
        } else {
            buf.putByte((byte)0xFF);
            switch (o) {
                case String s -> stringSerializer.serialize(s, buf);
                case Object[] arr -> arraySerializer.serialize(arr, buf, nullableElements, arrDim);
                default -> {
                    if (o.getClass().isArray()) {
                        // Primitive array
                        arraySerializer.serializePrimitive(o, buf);
                    } else {
                        objectSerializer.serialize(o, buf);
                    }
                }
            }
        }
    }

    public <T> T deserialize(ByteBuffer data, Class<T> clazz, boolean nullable, boolean[] nullableElements) {
        return deserialize(data, clazz, nullable, nullableElements, 0);
    }
    public <T> T deserialize(ByteBuffer data, Class<T> clazz, boolean nullable, boolean[] nullableElements, int arrDim) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class to deserialize cannot be null");
        }
        Log.i(TAG, "Deserializing class: " + clazz.getName());

        if (clazz.isPrimitive()) {
            return primitiveSerializer.deserialize(data, clazz);
        }

        byte nullByte = data.get();
        if (nullByte == (byte) 0x00) {
            if (!nullable) {
                throw new SerializationException("Found null value for non-nullable type " + clazz.getName());
            }
            Log.i(TAG, "Deserialized null value for class: " + clazz.getName());
            return null;
        } else if (nullByte != (byte) 0xFF) {
            throw new SerializationException("Invalid null byte value: " + String.format("0x%02X", nullByte));
        }
        if (isWrapper(clazz)) {
            return wrapperSerializer.deserialize(data, clazz);
        }
        if (clazz == String.class) {
            return clazz.cast(stringSerializer.deserialize(data));
        }
        if (clazz.isArray()) {
            return clazz.cast(arraySerializer.deserialize(data, clazz, nullableElements, arrDim));
        }
        return objectSerializer.deserialize(data, clazz);
    }
}
