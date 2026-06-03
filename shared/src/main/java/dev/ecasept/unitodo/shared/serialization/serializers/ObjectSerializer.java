package dev.ecasept.unitodo.shared.serialization.serializers;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.utils.Log;

import java.lang.reflect.Field;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectSerializer extends BaseSerializer {
    private static final String TAG = "ObjectSerializer";
    private static final Map<Class<?>, Field[]> fieldCache = new ConcurrentHashMap<>();

    private final DaddySerializer daddySerializer;

    public ObjectSerializer(DaddySerializer daddySerializer) {
        this.daddySerializer = daddySerializer;
    }

    static void ensureSerializable(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Serializable.class)) {
            throw new IllegalArgumentException("Tried to serialize non-serializable class " + clazz.getName());
        }
        try {
            clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Tried to serialize class without no-arg constructor " + clazz.getName());
        }
    }

    private <T> Field[] getFields(TypeContainer<T> type) {
        Class<?> clazz = type.getRawClass();
        return fieldCache.computeIfAbsent(clazz, c ->
                Arrays.stream(c.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(dev.ecasept.unitodo.shared.serialization.annotations.Field.class))
                        .peek(f -> {
                            validateField(c, f);
                            f.setAccessible(true);
                        })
                        .toArray(Field[]::new)
        );
    }

    private void validateField(Class<?> c, java.lang.reflect.Field f) {
        var annotation = f.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);

        if (annotation.nullable() && f.getType().isPrimitive()) {
            throw new IllegalArgumentException(
                    strField(c, f) + " is annotated as nullable but has primitive type " + f.getType().getName()
            );
        }

        boolean[] nullableElements = annotation.nullableElements();
        if (nullableElements == null || nullableElements.length == 0) return;

        if (!f.getType().isArray()) {
            throw new IllegalArgumentException(
                    strField(c, f) + " has nullableElements specified but is not an array type"
            );
        }

        Class<?> type = f.getType();
        int depth = 0;
        while (type.isArray()) {
            type = type.getComponentType();
            depth++;
        }

        if (nullableElements.length > depth) {
            throw new IllegalArgumentException(
                    strField(c, f) + " has nullableElements length " + nullableElements.length + " but array depth is " + depth
            );
        }

        if (nullableElements[nullableElements.length - 1] && type.isPrimitive()) {
            throw new IllegalArgumentException(
                    strField(c, f) + " declares nullableElements[" + (depth - 1)
                            + "]=true but base component type is primitive " + type.getName()
            );
        }
    }

    private static String strField(Class<?> c, java.lang.reflect.Field f) {
        return "Field '" + f.getName() + "' of class " + c.getName();
    }

    public void serialize(Object o, GrowableBuffer buf) {
        Class<?> clazz = o.getClass();
        Log.i(TAG, "Serializing custom object: " + clazz.getName());
        ensureSerializable(clazz);

        var lengthPos = buf.position();
        serializeLength(0, buf); // But default length for now
        int count = 0;
        HashSet<Integer> seenTags = new HashSet<>();
        for (Field field : getFields(new TypeContainer<>(clazz))) {
            Log.i(TAG, "Serializing field: " + field.getName() + " with type: " + field.getType().getName());
            try {
                var annotation = field.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
                int tag = annotation.tag();
                if (seenTags.contains(tag)) {
                    throw new SerializationException("Found duplicate tag " + tag + " while serializing object");
                }
                seenTags.add(tag);
                buf.putInt(tag);
                daddySerializer.serialize(field.get(o), field.getType(), buf, annotation.nullable(), annotation.nullableElements());
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Failed to access field " + field.getName() + " of class " + clazz.getName(), e);
            }
            count++;
        }
        // Write actual length
        serializeLength(count, lengthPos, buf);
    }

    public <T> T deserialize(ByteBuffer data, TypeContainer<T> type) {
        Class<?> clazz = type.getRawClass();
        ensureSerializable(clazz);

        // We want an object
        HashMap<Integer, Field> requiredTags = new HashMap<>();
        HashMap<Integer, Field> optionalTags = new HashMap<>();
        HashSet <Integer> seenTags = new HashSet<>();
        for (Field field : getFields(type)) {
            var annotation = field.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
            if (annotation.optional()) {
                optionalTags.put(annotation.tag(), field);
            } else {
                requiredTags.put(annotation.tag(), field);
            }
        }
        int count;
        try {
            count = deserializeLength(data);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read field count for object of class " + clazz.getName(), e);
        }
        if (count < 0) {
            throw new SerializationException("Negative field count " + count + " found while deserializing object of class " + clazz.getName());
        }
        Log.d(TAG, "count: " + count);
        T o = type.cast(instatiateSerializableObject(clazz));
        for (int i = 0; i < count; i++) {
            int tag;
            try {
                tag = data.getInt();
            } catch (BufferUnderflowException e) {
                throw new SerializationException("Unexpected end of data while trying to read tag for field " + (i + 1) + " of object of class " + clazz.getName(), e);
            }
            HashMap<Integer, Field> foundMap = null;
            if (requiredTags.containsKey(tag)) {
                foundMap = requiredTags;
            } else if (optionalTags.containsKey(tag)) {
                foundMap = optionalTags;
            }

            if (foundMap != null) {
                var field = foundMap.get(tag);
                foundMap.remove(tag);
                seenTags.add(tag);
                var annotation = field.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
                var val = daddySerializer.deserialize(data, type.getFieldType(field), annotation.nullable(), annotation.nullableElements());
                try {
                    field.set(o, val);
                    Log.i(TAG, "Deserialized field: " + field.getName() + " with value: " + val);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Failed to set field " + field.getName() + " of class " + clazz.getName(), e);
                }
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
