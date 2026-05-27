package dev.ecasept.unitodo.models.serialization.serializers;

import dev.ecasept.unitodo.models.serialization.GrowableBuffer;
import dev.ecasept.unitodo.models.serialization.SerializationException;
import dev.ecasept.unitodo.models.serialization.annotations.Serializable;
import dev.ecasept.unitodo.utils.Log;

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

    private Field[] getFields(Class<?> clazz) {
        return fieldCache.computeIfAbsent(clazz, c ->
                Arrays.stream(c.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(dev.ecasept.unitodo.models.serialization.annotations.Field.class))
                        .map(f -> {
                            boolean[] nullableElements = getBooleans(c, f);
                            if (nullableElements != null && nullableElements.length > 0) {
                                if (!f.getType().isArray()) {
                                    throw new IllegalArgumentException("Field '" + f.getName() + "' of class " + c.getName() + " has nullableElements specified but is not an array type");
                                }
                                Class<?> type = f.getType();
                                int depth = 0;
                                while (type.isArray()) {
                                    depth++;
                                    type = type.getComponentType();
                                }
                                if (nullableElements.length > depth) {
                                    throw new IllegalArgumentException("Field '" + f.getName() + "' of class " + c.getName() + " has nullableElements length " + nullableElements.length + " but array depth is " + depth);
                                }

                                type = f.getType();
                                for (int i = 0; i < nullableElements.length; i++) {
                                    type = type.getComponentType();
                                    if (nullableElements[i] && type.isPrimitive()) {
                                        throw new IllegalArgumentException("Field '" + f.getName() + "' of class " + c.getName() + " declares nullableElements[" + i + "]=true but component type at that level is primitive " + type.getName());
                                    }
                                }
                            }

                            f.setAccessible(true);
                            return f;
                        })
                        .toArray(Field[]::new)
        );
    }

    private static boolean[] getBooleans(Class<?> c, Field f) {
        var annotation = f.getAnnotation(dev.ecasept.unitodo.models.serialization.annotations.Field.class);
        // nullable() must not be used on primitive-typed fields
        if (annotation.nullable() && f.getType().isPrimitive()) {
            throw new IllegalArgumentException("Field '" + f.getName() + "' of class " + c.getName() + " is annotated as nullable but has primitive type " + f.getType().getName());
        }

        // If nullableElements is provided, validate against array depth and component types.
        // Index 0 corresponds to the outermost array component, last index to the base component.
        boolean[] nullableElements = annotation.nullableElements();
        return nullableElements;
    }

    public void serialize(Object o, GrowableBuffer buf) {
        Class<?> clazz = o.getClass();
        Log.i(TAG, "Serializing custom object: " + clazz.getName());
        ensureSerializable(clazz);

        var lengthPos = buf.position();
        serializeLength(0, buf); // But default length for now
        int count = 0;
        HashSet<Integer> seenTags = new HashSet<>();
        for (Field field : getFields(clazz)) {
            Log.i(TAG, "Serializing field: " + field.getName() + " with type: " + field.getType().getName());
            try {
                var annotation = field.getAnnotation(dev.ecasept.unitodo.models.serialization.annotations.Field.class);
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

    @SuppressWarnings("unchecked")
    public <T> T deserialize(ByteBuffer data, Class<T> clazz) {
        ensureSerializable(clazz);

        // We want an object
        HashMap<Integer, Field> requiredTags = new HashMap<>();
        HashMap<Integer, Field> optionalTags = new HashMap<>();
        HashSet <Integer> seenTags = new HashSet<>();
        for (Field field : getFields(clazz)) {
            var annotation = field.getAnnotation(dev.ecasept.unitodo.models.serialization.annotations.Field.class);
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
        T o = instatiateSerializableObject(clazz);
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
                var annotation = field.getAnnotation(dev.ecasept.unitodo.models.serialization.annotations.Field.class);
                var val = daddySerializer.deserialize(data, field.getType(), annotation.nullable(), annotation.nullableElements());
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
