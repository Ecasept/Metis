package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.compilers.DaddyCompiler;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;
import java.util.Optional;

public class OptionalAdapter<T> implements Adapter<Optional<T>> {
    private final DaddyCompiler daddyCompiler;

    public OptionalAdapter(DaddyCompiler daddyCompiler) {
        this.daddyCompiler = daddyCompiler;
    }

    @Override
    public Schema<Optional<T>> compileToSchema(NullableTypeContainer<Optional<T>> nullableType) {
        //noinspection unchecked
        var childType = (TypeContainer<T>) nullableType.type().getGenericArgument(0);
        var childSchema = daddyCompiler.compileToSchema(NullableTypeContainer.of(childType, false));
        if (nullableType.nullable()) {
            throw new IllegalArgumentException("Optional type cannot be nullable");
        }
        return new Schema<>() {
            @Override
            public void serialize(Optional<T> obj, GrowableBuffer buf) {
                if (obj.isPresent()) {
                    buf.putByte((byte) 0xFF);
                    childSchema.serialize(obj.get(), buf);
                } else {
                    buf.putByte((byte) 0x00);
                }
            }

            @Override
            public Optional<T> deserialize(ByteBuffer data) throws SerializationException {
                byte b = data.get();
                if (b == (byte) 0xFF) {
                    T value = childSchema.deserialize(data);
                    return Optional.of(value);
                } else if (b == (byte) 0x00) {
                    return Optional.empty();
                } else {
                    throw new SerializationException("Encountered invalid Optional byte value: " + b);
                }
            }
        };
    }
}
