package dev.ecasept.unitodo.shared.serialization.schemas;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record StringSchema(boolean nullable) implements Schema<String> {
    private static final String TAG = "StringSchema";
    @Override
    public void serialize(String s, GrowableBuffer buf) {
        Log.i(TAG, "Serializing string: " + s);
        if (nullable && serializeNullable(s, buf)) {
            return;
        }
        var bytes = s.getBytes(StandardCharsets.UTF_8);
        SerializationUtils.serializeLength(bytes.length, buf);
        buf.putBytes(bytes);
    }

    @Override
    public String deserialize(ByteBuffer data) throws SerializationException {
        Log.i(TAG, "Deserializing string");
        if (nullable && deserializeNullable(data)) {
            return null;
        }
        int len = SerializationUtils.deserializeLength(data);
        byte[] bytes = new byte[len];
        try {
            data.get(bytes);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Not enough data to read string of length " + len, e);
        }
        String s = new String(bytes, StandardCharsets.UTF_8);
        Log.i(TAG, "Deserialized string: " + s);
        return s;
    }
}