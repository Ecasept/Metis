package dev.ecasept.unitodo.models.serialization.serializers;

import dev.ecasept.unitodo.models.serialization.GrowableBuffer;
import dev.ecasept.unitodo.models.serialization.SerializationException;
import dev.ecasept.unitodo.utils.Log;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringSerializer extends BaseSerializer {
    private static final String TAG = "StringSerializer";
    public void serialize(String s, GrowableBuffer buf) {
        Log.i(TAG, "Serializing string: " + s);
        var bytes = s.getBytes(StandardCharsets.UTF_8);
        serializeLength(bytes.length, buf);
        buf.putBytes(bytes);
    }
    public String deserialize(ByteBuffer data) {
        int len = deserializeLength(data);
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