package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.compilers.DaddyCompiler;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDAdapter implements Adapter<UUID> {
    private static final String TAG = "UUIDAdapter";

    private final DaddyCompiler daddyCompiler;

    public UUIDAdapter(DaddyCompiler daddyCompiler) {
        this.daddyCompiler = daddyCompiler;
    }

    @Override
    public Schema<UUID> compileToSchema(NullableTypeContainer<UUID> nullableType) {
        var stringSchema = daddyCompiler.compileToSchema(NullableTypeContainer.of(String.class, nullableType.nullable()));

        return new Schema<>() {
            @Override
            public void serialize(UUID uuid, GrowableBuffer buf) {
                String s = uuid.toString();
                stringSchema.serialize(s, buf);
            }

            @Override
            public UUID deserialize(ByteBuffer data) throws SerializationException {
                String s = stringSchema.deserialize(data);
                try {
                    return UUID.fromString(s);
                } catch (IllegalArgumentException e) {
                    throw new SerializationException("Failed to deserialize UUID from string: " + s, e);
                }
            }
        };
    }
}