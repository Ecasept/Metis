package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;

public record EnumSchema<T>(HashMap<T, Integer> enumToTag, boolean nullable) implements Schema<T> {
    private static final String TAG = "EnumSchema";
    @Override
    public void serialize(T value, GrowableBuffer buf) {
        Log.i(TAG, "Serializing enum value: " + value);
        if (nullable && serializeNullable(value, buf)) {
            return;
        }
        if (!enumToTag.containsKey(value)) {
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }
        int tag = enumToTag.get(value);
        buf.putInt(tag);
    }
    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        Log.i(TAG, "Deserializing enum value");
        if (nullable && deserializeNullable(data)) {
            return null;
        }
        int tag = data.getInt();
        for (var entry : enumToTag.entrySet()) {
            if (entry.getValue() == tag) {
                Log.i(TAG, "Deserialized enum value: " + entry.getKey() + " with tag: " + tag);
                return entry.getKey();
            }
        }
        throw new SerializationException("Unknown enum tag: " + String.format("0x%02X", tag));
    }
}