package dev.ecasept.unitodo.shared.serialization.schemas.object;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/** Includes the instructions for how to (de-)serialize fields of an object */
public record FieldSchema<T>(Field field, Schema<T> schema, boolean optional, int tag) implements Schema<T> {
    @Override
    public void serialize(T o, GrowableBuffer buf) {
        schema.serialize(o, buf);
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        return schema.deserialize(data);
    }

    public T getValue(Object obj) throws IllegalAccessException {
        //noinspection unchecked
        return (T) field.get(obj);
    }
    public void setValue(Object obj, T value) throws IllegalAccessException {
        field.set(obj, value);
    }
}

