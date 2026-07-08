package dev.ecasept.unitodo.shared.serialization.schemas.object;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.schemas.SerializationUtils;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Includes the instructions for how to (de-)serialize objects */
public record ObjectSchema<T>(NullableTypeContainer<T> nullableType, ArrayList<FieldSchema<?>> fieldSchemas) implements Schema<T> {
    private static final String TAG = "ObjectSerializer";

    private static <T> void serializeField(FieldSchema<T> field, Object o, GrowableBuffer buf, Class<?> clazz) {
        Log.i(TAG, "Serializing field: " + field.field().getName() + " with type: " + field.field().getType().getName());
        try {
            field.serialize(field.getValue(o), buf);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to access field " + field.field().getName() + " of class " + clazz.getName(), e);
        }
    }

    private static <T> void deserializeField(FieldSchema<T> field, Object o, ByteBuffer buf, Class<?> clazz) throws SerializationException {
        Log.i(TAG, "Deserializing field: " + field.field().getName() + " with type: " + field.field().getType().getName());
        try {
            field.setValue(o, field.deserialize(buf));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Failed to access field " + field.field().getName() + " of class " + clazz.getName(), e);
        }
    }

    @Override
    public void serialize(T o, GrowableBuffer buf) {
        Class<?> clazz = o.getClass();
        Log.i(TAG, "Serializing object: " + clazz.getName());
        if (clazz != nullableType.type().getRawClass()) {
            throw new IllegalArgumentException("Object of type " + clazz.getName() + " does not match expected type " + nullableType.type().getRawClass().getName());
        }

        var lengthPos = buf.position();
        SerializationUtils.serializeLength(0, buf); // But default length for now
        int count = 0;
        HashSet<Integer> seenTags = new HashSet<>();
        for (var field : fieldSchemas) {
            buf.putInt(field.tag());
            serializeField(field, o, buf, clazz);
            count++;
        }
        // Write actual length
        SerializationUtils.serializeLength(count, lengthPos, buf);
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        Class<?> clazz = nullableType.type().getRawClass();
        Log.i(TAG, "Deserializing object: " + clazz.getName());

        HashMap<Integer, FieldSchema<?>> requiredTags = new HashMap<>();
        HashMap<Integer, FieldSchema<?>> optionalTags = new HashMap<>();
        HashSet<Integer> seenTags = new HashSet<>();

        for (var field : fieldSchemas) {
            if (field.optional()) {
                optionalTags.put(field.tag(), field);
            } else {
                requiredTags.put(field.tag(), field);
            }
        }

        int count;
        try {
            count = SerializationUtils.deserializeLength(data);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read field count for object of class " + clazz.getName(), e);
        }
        if (count < 0) {
            throw new SerializationException("Negative field count " + count + " found while deserializing object of class " + clazz.getName());
        }

        T o = nullableType.type().cast(SerializationUtils.instantiateSerializableObject(clazz));

        for (int i = 0; i < count; i++) {
            int tag;
            try {
                tag = data.getInt();
            } catch (BufferUnderflowException e) {
                throw new SerializationException("Unexpected end of data while trying to read tag for field " + (i + 1) + " of object of class " + clazz.getName(), e);
            }

            HashMap<Integer, FieldSchema<?>> foundMap = null;
            if (requiredTags.containsKey(tag)) {
                foundMap = requiredTags;
            } else if (optionalTags.containsKey(tag)) {
                foundMap = optionalTags;
            }

            if (foundMap != null) {
                var field = foundMap.get(tag);
                foundMap.remove(tag);
                seenTags.add(tag);
                deserializeField(field, o, data, clazz);
            } else if (seenTags.contains(tag)) {
                throw new SerializationException("Duplicate tag " + tag + " found while deserializing object");
            } else {
                throw new SerializationException("Unknown tag " + tag + " found while deserializing object");
            }
        }

        if (!requiredTags.isEmpty()) {
            throw new SerializationException("Some required tags were found missing during object deserialization: " + requiredTags);
        }

        return o;
    }
}
