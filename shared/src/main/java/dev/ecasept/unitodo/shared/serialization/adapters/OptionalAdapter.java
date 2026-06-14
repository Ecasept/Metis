package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;
import java.util.Optional;

public class OptionalAdapter<T> extends Adapter<Optional<T>> {
    public OptionalAdapter(DaddySerializer daddySerializer) {
        super(daddySerializer);
    }

    @Override
    public void serialize(Optional<T> obj, GrowableBuffer buf) {
        if (obj.isPresent()) {
            buf.putByte((byte) 0xFF);
            daddySerializer.serialize(obj.get(), null, buf, false, new boolean[0]);
        } else {
            buf.putByte((byte) 0x00);
        }
    }

    @Override
    public Optional<T> deserialize(ByteBuffer data, TypeContainer<Optional<T>> type) throws SerializationException {
        byte b = data.get();
        if (b == (byte) 0xFF) {
            @SuppressWarnings("unchecked")
            TypeContainer<T> subType = (TypeContainer<T>) type.getGenericArgument(0);
            T value = daddySerializer.deserialize(data, subType, false, new boolean[0]);
            return Optional.of(value);
        } else if (b == (byte) 0x00) {
            return Optional.empty();
        } else {
            throw new SerializationException("Encountered invalid Optional byte value: " + b);
        }
    }
}
