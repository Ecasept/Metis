package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.shared.serialization.serializers.StringSerializer;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.UUID;

public class UUIDAdapter extends Adapter<UUID> {
    private static final String TAG = "UUIDAdapter";
    private static final StringSerializer stringSerializer = new StringSerializer();
    public UUIDAdapter(DaddySerializer daddySerializer) {
        super(daddySerializer);
    }

    @Override
    public void serialize(UUID uuid, GrowableBuffer buf) {
        Log.i(TAG, "Serializing UUID: " + uuid.toString());
        String s = uuid.toString();
        stringSerializer.serialize(s, buf);

    }

    @Override
    public UUID deserialize(ByteBuffer data, TypeContainer<UUID> type) throws SerializationException {
        String s = stringSerializer.deserialize(data);
        Log.i(TAG, "Deserialized UUID string: " + s);
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            throw new SerializationException("Failed to deserialize UUID from string: " + s, e);
        }
    }
}
