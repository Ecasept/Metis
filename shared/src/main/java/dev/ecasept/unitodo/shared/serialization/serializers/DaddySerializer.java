package dev.ecasept.unitodo.shared.serialization.serializers;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.serialization.adapters.Adapter;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class DaddySerializer extends BaseSerializer {
    public static final String TAG = "DaddySerializer";

    private final ArraySerializer arraySerializer = new ArraySerializer(this);
    private final PrimitiveSerializer primitiveSerializer = new PrimitiveSerializer();
    private final StringSerializer stringSerializer = new StringSerializer();
    private final ObjectSerializer objectSerializer = new ObjectSerializer(this);
    private final Map<Class<?>, ? extends Adapter<?>> adapters;

    public DaddySerializer(HashMap<Class<?>, Class<? extends Adapter<?>>> adapters) {
        this.adapters = adapters.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> {
                    var v = e.getValue();
            try {
                return v.getConstructor(DaddySerializer.class).newInstance(this);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to instantiate adapter " + v.getName() + " for class " + e.getKey().getName(), ex);
            }
        }));
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
        if (clazz == Void.class || clazz == void.class) {
            return; // This allows us to serialize empty content, e.g. for empty GET requests
        }
        if (clazz.isPrimitive()) {
            primitiveSerializer.serialize(o, buf);
        } else if (isWrapper(clazz)) {
            if (nullable) {
                buf.putByte((byte)0xFF);
            }
            primitiveSerializer.serialize(o, buf);
        } else {
            if (nullable) {
                buf.putByte((byte)0xFF);
            }
            switch (o) {
                case String s -> stringSerializer.serialize(s, buf);
                case Object[] arr -> arraySerializer.serialize(arr, buf, nullableElements, arrDim);
                default -> {
                    if (o.getClass().isArray()) {
                        // Primitive array
                        arraySerializer.serializePrimitive(o, buf);
                    } else if (adapters.containsKey(clazz)) {
                        @SuppressWarnings("unchecked")
                        var adapter = (Adapter<Object>) adapters.get(clazz);
                        adapter.serialize(o, buf);
                    } else {
                        objectSerializer.serialize(o, buf);
                    }
                }
            }
        }
    }

    public <T> T deserialize(ByteBuffer data, TypeContainer<T> type, boolean nullable, boolean[] nullableElements) {
        return deserialize(data, type, nullable, nullableElements, 0);
    }
    public <T> T deserialize(ByteBuffer data, TypeContainer<T> type, boolean nullable, boolean[] nullableElements, int arrDim) {

        Log.i(TAG, "Deserializing type: " + type.getTypeName());
        if (type.isVoid()) {
            return null; // This allows us to deserialize empty content, e.g. for empty GET requests
        }

        if (type.isPrimitive()) {
            return type.cast(primitiveSerializer.deserialize(data, type.asClass()));
        }

        if (nullable) {
            byte nullByte = data.get();
            if (nullByte == (byte) 0x00) {
                Log.i(TAG, "Deserialized null value for type: " + type.getTypeName());
                return null;
            } else if (nullByte != (byte) 0xFF) {
                throw new SerializationException("Invalid null byte value: " + String.format("0x%02X", nullByte));
            }
        }
        if (type.isWrapper()) {
            return type.cast(primitiveSerializer.deserialize(data, type.asClass()));
        }
        if (type.isString()) {
            return type.cast(stringSerializer.deserialize(data));
        }
        if (type.isArray()) {
            return type.cast(arraySerializer.deserialize(data, type, nullableElements, arrDim));
        }
        if (type.isClass()) {
            if (adapters.containsKey(type.asClass())) {
                var adapter = adapters.get(type.asClass());
                return type.cast(adapter.deserialize(data));
            }
        }
        if (type.isWildcard() || type.isTypeVariable()) {
            throw new IllegalArgumentException("Cannot deserialize wildcard or type variable types: " + type.getTypeName());
        }
        return objectSerializer.deserialize(data, type);
    }
}
