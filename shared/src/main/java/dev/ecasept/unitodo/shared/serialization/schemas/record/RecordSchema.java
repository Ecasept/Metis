package dev.ecasept.unitodo.shared.serialization.schemas.record;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.schemas.SerializationUtils;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** Includes the instructions for how to (de-)serialize records */
public record RecordSchema<T>(NullableTypeContainer<T> nullable, ArrayList<RecordComponentSchema<?>> componentSchemas, Constructor<T> canonicalCtor) implements Schema<T> {
    private static final String TAG = "RecordSerializer";

    private static <T> void serializeComponent(RecordComponentSchema<T> component, Object record, GrowableBuffer buf) throws IllegalAccessException, InvocationTargetException {
        component.serialize(component.getValue(record), buf);
    }

    @Override
    public void serialize(T record, GrowableBuffer buf) {
        Class<?> clazz = record.getClass();
        Log.i(TAG, "Serializing record: " + clazz.getName());
        if (clazz != nullable.type().asClass()) {
            throw new IllegalArgumentException("Record of type " + clazz.getName() + " does not match expected type " + nullable.type().asClass().getName());
        }
        var lengthPos = buf.position();
        SerializationUtils.serializeLength(0, buf);
        int count = 0;
        for (var component : componentSchemas) {
            try {
                Log.i(TAG, "Serializing record component: " + component.component().getName() + " with tag: " + component.tag());
                buf.putInt(component.tag());
                serializeComponent(component, record, buf);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Failed to access component " + component.component().getName() + " of record " + clazz.getName(), e);
            }
            count++;
        }
        SerializationUtils.serializeLength(count, lengthPos, buf);
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        Class<?> clazz = nullable.type().asClass();
        Log.i(TAG, "Deserializing record: " + clazz.getName());

        int componentCount = componentSchemas.size();

        // Build maps for quick lookup
        HashMap<Integer, RecordComponentSchema<?>> tagToComponent = new HashMap<>();
        HashMap<Integer, Integer> tagToConstructorIndex = new HashMap<>();
        HashSet<Integer> requiredTags = new HashSet<>();

        for (int i = 0; i < componentCount; i++) {
            var component = componentSchemas.get(i);
            int tag = component.tag();
            tagToComponent.put(tag, component);
            tagToConstructorIndex.put(tag, i);
            requiredTags.add(tag);
        }

        int count;
        try {
            count = SerializationUtils.deserializeLength(data);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read component count for record " + clazz.getName(), e);
        }
        if (count < 0) {
            throw new SerializationException("Negative component count " + count + " found while deserializing record " + clazz.getName());
        }

        Object[] constructorArgs = new Object[componentCount];
        HashSet<Integer> seenTags = new HashSet<>();

        for (int i = 0; i < count; i++) {
            int tag;
            try {
                tag = data.getInt();
            } catch (BufferUnderflowException e) {
                throw new SerializationException("Unexpected end of data while trying to read tag for component " + (i + 1) + " of record " + clazz.getName(), e);
            }

            if (tagToComponent.containsKey(tag)) {
                var component = tagToComponent.get(tag);
                int index = tagToConstructorIndex.get(tag);

                requiredTags.remove(tag);
                seenTags.add(tag);

                Log.i(TAG, "Deserializing record component: " + component.component().getName() + " with tag: " + tag);
                Object val = component.deserialize(data);
                constructorArgs[index] = val;
            } else if (seenTags.contains(tag)) {
                throw new SerializationException("Duplicate tag " + tag + " found while deserializing record " + clazz.getName());
            } else {
                throw new SerializationException("Unknown tag " + tag + " found while deserializing record " + clazz.getName());
            }
        }

        if (!requiredTags.isEmpty()) {
            throw new SerializationException("Some required tags were found missing during record deserialization: " + requiredTags);
        }

        try {
            return canonicalCtor.newInstance(constructorArgs);
        } catch (Exception e) {
            throw new SerializationException("Failed to instantiate record " + clazz.getName() + " via its canonical constructor", e);
        }
    }
}
