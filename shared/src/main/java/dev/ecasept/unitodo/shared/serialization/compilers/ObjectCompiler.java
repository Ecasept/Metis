package dev.ecasept.unitodo.shared.serialization.compilers;

import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.serialization.schemas.object.FieldSchema;
import dev.ecasept.unitodo.shared.serialization.schemas.object.ObjectSchema;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;

public class ObjectCompiler {
    private final DaddyCompiler daddyCompiler;
    public ObjectCompiler(DaddyCompiler daddyCompiler) {
        this.daddyCompiler = daddyCompiler;
    }

    static void ensureSerializable(Class<?> clazz) {
        if (clazz.isAnonymousClass()) {
            throw new IllegalArgumentException("Anonymous classes are not serializable: " + clazz.getName());
        }
        if (clazz.isLocalClass()) {
            throw new IllegalArgumentException("Local classes are not serializable: " + clazz.getName());
        }
        if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
            throw new IllegalArgumentException("Non-static inner classes are not serializable (implicit outer reference): " + clazz.getName());
        }
        if (clazz.isSynthetic()) {
            throw new IllegalArgumentException("Synthetic classes are not serializable: " + clazz.getName());
        }
        if (Proxy.isProxyClass(clazz)) {
            throw new IllegalArgumentException("Dynamic proxy classes are not serializable: " + clazz.getName());
        }
        if (clazz.isEnum() || clazz.isInterface() || clazz.isArray() || clazz.isPrimitive()) {
            throw new IllegalStateException("Class " + clazz.getName() + " should have been handled by an earlier stage");
        }

        if (!clazz.isAnnotationPresent(Serializable.class)) {
            throw new IllegalArgumentException("Tried to (de)serialize non-serializable class " + clazz.getName());
        }
        try {
            clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Tried to (de)serialize class without no-arg constructor " + clazz.getName());
        }
    }

    public <T> ObjectSchema<T> compileToSchema(NullableTypeContainer<T> nullableType) {
        var type = nullableType.type();
        var clazz = type.getRawClass();
        ensureSerializable(clazz);

        var schemas = new ArrayList<FieldSchema<?>>();

        HashSet<Integer> seenTags = new HashSet<>();
        for (var field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(dev.ecasept.unitodo.shared.serialization.annotations.Field.class)) {
                throw new IllegalArgumentException("Field " + field.getName() + " of class " + clazz.getName() + " is not annotated with @Field");
            }
            field.setAccessible(true);
            var annotation = field.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
            if (seenTags.contains(annotation.tag())) {
                throw new IllegalArgumentException("Found duplicate tag " + annotation.tag() + " in class " + clazz.getName());
            }
            seenTags.add(annotation.tag());
            var fieldType = new NullableTypeContainer<>(type.getFieldType(field), annotation.nullable(), annotation.nullableElements(), 0);

            var schema = daddyCompiler.compileToSchema(fieldType);
            schemas.add(new FieldSchema<>(field, schema, annotation.optional(), annotation.tag()));
        }
        return new ObjectSchema<>(nullableType, schemas);
    }
}
