package dev.ecasept.unitodo.shared.serialization.compilers;

import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.schemas.record.RecordComponentSchema;
import dev.ecasept.unitodo.shared.serialization.schemas.record.RecordSchema;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashSet;

public class RecordCompiler {
    private final DaddyCompiler daddyCompiler;

    public RecordCompiler(DaddyCompiler daddyCompiler) {
        this.daddyCompiler = daddyCompiler;
    }

    static void ensureSerializable(Class<?> clazz) {
        if (!clazz.isRecord()) {
            throw new IllegalStateException("Class " + clazz.getName() + " is not a record");
        }
        if (!clazz.isAnnotationPresent(Serializable.class)) {
            throw new IllegalArgumentException("Tried to (de)serialize non-serializable record " + clazz.getName());
        }
    }

    public <T> RecordSchema<T> compileToSchema(NullableTypeContainer<T> nullableType) {
        var type = nullableType.type();
        var clazz = type.asClass();
        ensureSerializable(clazz);

        RecordComponent[] components = clazz.getRecordComponents();
        var schemas = new ArrayList<RecordComponentSchema<?>>();
        HashSet<Integer> seenTags = new HashSet<>();

        Class<?>[] paramTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
        }

        Constructor<T> canonicalCtor;
        try {
            @SuppressWarnings("unchecked")
            Constructor<T> ctor = (Constructor<T>) clazz.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            canonicalCtor = ctor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find canonical constructor for record " + clazz.getName(), e);
        }

        for (var component : components) {
            if (!component.isAnnotationPresent(dev.ecasept.unitodo.shared.serialization.annotations.Field.class)) {
                throw new IllegalArgumentException("Component " + component.getName() + " of record " + clazz.getName() + " is not annotated with @Field");
            }
            var annotation = component.getAnnotation(dev.ecasept.unitodo.shared.serialization.annotations.Field.class);
            if (seenTags.contains(annotation.tag())) {
                throw new IllegalArgumentException("Found duplicate tag " + annotation.tag() + " in record " + clazz.getName());
            }
            seenTags.add(annotation.tag());

            if (annotation.optional()) {
                throw new IllegalArgumentException("Record component " + component.getName() + " of " + clazz.getName() + " cannot be optional");
            }

            var componentType = NullableTypeContainer.of(
                    type.getRecordComponentType(component),
                    annotation.nullable(),
                    annotation.nullableElements()
            );
            Schema<?> schema = daddyCompiler.compileToSchema(componentType);
            schemas.add(new RecordComponentSchema<>(component, schema, annotation.tag()));
        }

        return new RecordSchema<>(nullableType, schemas, canonicalCtor);
    }
}