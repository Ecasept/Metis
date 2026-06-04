package dev.ecasept.unitodo.shared.serialization.serializers;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.utils.Log;

import java.lang.reflect.*;
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
    private static final Map<Class<?>, RecordComponent[]> recordComponentCache = new ConcurrentHashMap<>();

    private final DaddySerializer daddySerializer;

    public ObjectSerializer(DaddySerializer daddySerializer) {
        this.daddySerializer = daddySerializer;
    }

    static void ensureSerializable(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Serializable.class)) {
            throw new IllegalArgumentException("Tried to (de)serialize non-serializable class " + clazz.getName());
        }
        try {
            if (clazz.isRecord()) {
                // Ensure that every record component is annotated with @Field and that the record has canonical constructor
                for (RecordComponent component : clazz.getRecordComponents()) {
                    if (!component.isAnnotationPresent(dev.ecasept.unitodo.shared.serialization.annotations.Field.class)) {
                        throw new IllegalArgumentException("Record component " + component.getName() + " of record " + clazz.getName() + " is not annotated with @Field");
                    }
                    var annotation = component.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
                    if (annotation.optional()) {
                        throw new IllegalArgumentException("Record component " + component.getName() + " of record " + clazz.getName() + " is annotated as optional, which is not supported for record components");
                    }
                }
            } else {
                clazz.getDeclaredConstructor();
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Tried to (de)serialize class without no-arg constructor " + clazz.getName());
        }
    }

    private Field[] getFields(Class<?> clazz) {
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
    private RecordComponent[] getRecordComponents(Class<?> clazz) {
        return recordComponentCache.computeIfAbsent(clazz, c ->
                Arrays.stream(c.getRecordComponents())
                        .filter(f -> f.isAnnotationPresent(dev.ecasept.unitodo.shared.serialization.annotations.Field.class))
                        .peek(f -> validateField(c, f))
                        .toArray(RecordComponent[]::new)
        );
    }

    private void validateField(Class<?> c, AnnotatedElement f) {
        var annotation = f.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);

        Class<?> type = switch (f) {
            case Field field -> field.getType();
            case RecordComponent component -> component.getType();
            default -> throw new IllegalArgumentException("Unsupported element type: " + f.getClass());        };

        if (annotation.nullable() && type.isPrimitive()) {
            throw new IllegalArgumentException(
                    strField(c, f) + " is annotated as nullable but has primitive type " + type.getName()
            );
        }

        boolean[] nullableElements = annotation.nullableElements();
        if (nullableElements == null || nullableElements.length == 0) return;

        if (!type.isArray()) {
            throw new IllegalArgumentException(
                    strField(c, f) + " has nullableElements specified but is not an array type"
            );
        }

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

    private static String strField(Class<?> c, AnnotatedElement f) {
        var name = switch (f) {
            case Field field -> field.getName();
            case RecordComponent component -> component.getName();
            default -> throw new IllegalArgumentException("Unsupported element type: " + f.getClass());
        };
        return "Field '" + name + "' of class " + c.getName();
    }

    public <T> void serializeRecord(T o, GrowableBuffer buf) {
        Class<?> clazz = o.getClass();

        var components = getRecordComponents(clazz);
        serializeLength(components.length, buf);

        HashSet<Integer> seenTags = new HashSet<>();
        for (RecordComponent component : components) {
            Log.i(TAG, "Serializing record component: " + component.getName() + " with type: " + component.getType().getName());

            try {
                var annotation = component.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
                int tag = annotation.tag();
                if (seenTags.contains(tag)) {
                    throw new IllegalArgumentException("Found duplicate tag " + tag + " while serializing record");
                }
                seenTags.add(tag);
                buf.putInt(tag);
                daddySerializer.serialize(component.getAccessor().invoke(o), component.getType(), buf, annotation.nullable(), annotation.nullableElements());
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IllegalArgumentException("Failed to access component " + component.getName() + " of record " + clazz.getName(), e);
            }

        }
    }

    public <T> void serialize(T o, GrowableBuffer buf) {
        Class<?> clazz = o.getClass();
        Log.i(TAG, "Serializing custom object: " + clazz.getName());
        ensureSerializable(clazz);
        if (clazz.isRecord()) {
            serializeRecord(o, buf);
            return;
        }

        var lengthPos = buf.position();
        serializeLength(0, buf); // But default length for now
        int count = 0;
        HashSet<Integer> seenTags = new HashSet<>();
        for (Field field : getFields(clazz)) {
            Log.i(TAG, "Serializing field: " + field.getName() + " with type: " + field.getType().getName());
            try {
                var annotation = field.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
                int tag = annotation.tag();
                if (seenTags.contains(tag)) {
                    throw new IllegalArgumentException("Found duplicate tag " + tag + " while serializing object");
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

    private <T> T deserializeRecord(ByteBuffer data, TypeContainer<T> type) throws SerializationException {
        Class<?> clazz = type.getRawClass();
        RecordComponent[] components = clazz.getRecordComponents();
        int componentCount = components.length;

        // Build maps for quick lookup
        HashMap<Integer, RecordComponent> tagToComponent = new HashMap<>();
        HashMap<Integer, Integer> tagToConstructorIndex = new HashMap<>();
        HashSet<Integer> requiredTags = new HashSet<>();

        for (int i = 0; i < componentCount; i++) {
            RecordComponent comp = components[i];
            var annotation = comp.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
            int tag = annotation.tag();

            tagToComponent.put(tag, comp);
            tagToConstructorIndex.put(tag, i);
            requiredTags.add(tag);
        }

        // Read count
        int count;
        try {
            count = deserializeLength(data);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read field count for record of class " + clazz.getName(), e);
        }

        Object[] constructorArgs = new Object[componentCount];
        HashSet<Integer> seenTags = new HashSet<>();

        // go through all fields in the data and match them to record components via their tags
        for (int i = 0; i < count; i++) {
            int tag;
            try {
                tag = data.getInt();
            } catch (BufferUnderflowException e) {
                throw new SerializationException("Unexpected end of data while trying to read tag for component " + (i + 1) + " of record " + clazz.getName(), e);
            }

            if (tagToComponent.containsKey(tag)) {
                RecordComponent component = tagToComponent.get(tag);
                int index = tagToConstructorIndex.get(tag);

                requiredTags.remove(tag);
                seenTags.add(tag);

                var annotation = component.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);

                var componentType = type.getRecordComponentType(component);
                Object val = daddySerializer.deserialize(data, componentType, annotation.nullable(), annotation.nullableElements());

                constructorArgs[index] = val;
                Log.i(TAG, "Deserialized record component: " + component.getName() + " at index [" + index + "] with value: " + val);

            } else if (seenTags.contains(tag)) {
                throw new SerializationException("Duplicate tag " + tag + " found while deserializing record " + clazz.getName());
            } else {
                throw new SerializationException("Unknown tag " + tag + " found while deserializing record " + clazz.getName());
            }
        }

        if (!requiredTags.isEmpty()) {
            throw new SerializationException("Some required tags were found missing during record deserialization: " + requiredTags);
        }

        // Instantiate record
        try {
            Class<?>[] paramTypes = Arrays.stream(components)
                    .map(RecordComponent::getType)
                    .toArray(Class[]::new);

            @SuppressWarnings("unchecked")
            Constructor<T> constructor = (Constructor<T>) clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);

            return constructor.newInstance(constructorArgs);
        } catch (Exception e) {
            throw new SerializationException("Failed to instantiate record " + clazz.getName() + " via its canonical constructor", e);
        }
    }

    public <T> T deserialize(ByteBuffer data, TypeContainer<T> type) throws SerializationException {
        Class<?> clazz = type.getRawClass();
        ensureSerializable(clazz);
        if (clazz.isRecord()) {
            return deserializeRecord(data, type);
        }

        // We want an object
        HashMap<Integer, Field> requiredTags = new HashMap<>();
        HashMap<Integer, Field> optionalTags = new HashMap<>();
        HashSet <Integer> seenTags = new HashSet<>();
        for (Field field : getFields(clazz)) {
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
