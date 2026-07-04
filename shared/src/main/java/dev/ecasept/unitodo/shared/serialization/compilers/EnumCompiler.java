package dev.ecasept.unitodo.shared.serialization.compilers;

import dev.ecasept.unitodo.shared.serialization.annotations.SerialInstance;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.serialization.schemas.EnumSchema;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

public class EnumCompiler {

    public <T> EnumSchema<T> compileToSchema(NullableTypeContainer<T> nullableType) {
        var type = nullableType.type();
        var clazz = type.asClass();

        if (!clazz.isAnnotationPresent(Serializable.class)) {
            throw new IllegalArgumentException("Tried to (de)serialize non-serializable enum " + clazz.getName());
        }

        HashSet<Integer> seenTags = new HashSet<>();
        HashMap<T, Integer> enumConstantToTagMap = new HashMap<>();
        for (var enumConstant : clazz.getEnumConstants()) {
            var annotation = getAnnotation(enumConstant, clazz);
            if (seenTags.contains(annotation.tag())) {
                throw new IllegalArgumentException("Found duplicate tag " + annotation.tag() + " in enum " + clazz.getName());
            }
            seenTags.add(annotation.tag());
            //noinspection unchecked
            enumConstantToTagMap.put((T) enumConstant, annotation.tag());
        }
        return new EnumSchema<>(enumConstantToTagMap, nullableType.nullable());
    }

    private static SerialInstance getAnnotation(Object enumConstant, Class<?> clazz) {
        Field field;
        try {
            field = clazz.getField(((Enum<?>) enumConstant).name());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Could not find field for enum constant " + enumConstant + " of class " + clazz.getName(), e);
        }
        if (!field.isAnnotationPresent(SerialInstance.class)) {
            throw new IllegalArgumentException("Enum constant " + field.getName() + " of class " + clazz.getName() + " is not annotated with @SerialInstance");
        }
        return field.getAnnotation(SerialInstance.class);
    }
}
