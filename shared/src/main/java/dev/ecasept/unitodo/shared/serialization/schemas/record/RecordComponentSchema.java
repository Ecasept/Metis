package dev.ecasept.unitodo.shared.serialization.schemas.record;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.ByteBuffer;

public record RecordComponentSchema<T>(RecordComponent component, Schema<T> schema, int tag) implements Schema<T> {
    public T getValue(Object record) throws IllegalAccessException, InvocationTargetException {
        //noinspection unchecked
        return (T) component.getAccessor().invoke(record);
    }

    @Override
    public void serialize(T value, GrowableBuffer buf) {
        schema.serialize(value, buf);
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        return schema.deserialize(data);
    }
}
