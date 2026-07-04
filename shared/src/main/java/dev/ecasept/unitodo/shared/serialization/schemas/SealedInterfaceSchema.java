package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;
import java.util.Map;

public record SealedInterfaceSchema<T>(Map<Class<?>, Implementation<?>> implementationsByClass, boolean nullable) implements Schema<T> {

    public record Implementation<R>(int tag, Schema<R> schema) {}

    @Override
    public void serialize(T value, GrowableBuffer buf) {
        Log.i("SealedInterfaceSchema", "Serializing sealed interface");
        if (nullable && serializeNullable(value, buf)) {
            return;
        }
        if (!implementationsByClass.containsKey(value.getClass())) {
            throw new IllegalArgumentException("Unknown sealed interface implementation: " + value.getClass().getName());
        }
        //noinspection unchecked
        var implementation = (Implementation<T>) implementationsByClass.get(value.getClass());
        buf.putInt(implementation.tag());
        implementation.schema().serialize(value, buf);
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        Log.i("SealedInterfaceSchema", "Deserializing sealed interface");
        if (nullable && deserializeNullable(data)) {
            return null;
        }
        int tag = data.getInt();
        //noinspection unchecked
        var implementation = (Implementation<T>) implementationsByClass.values().stream()
                .filter(v -> v.tag() == tag)
                .findFirst()
                .orElseThrow(() -> new SerializationException("Unknown sealed interface tag " + tag));
        return implementation.schema().deserialize(data);
    }
}