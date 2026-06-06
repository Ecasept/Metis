package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.adapters.Adapter;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;

public class ApiResponseAdapter<T> extends Adapter<ApiResponse<T>> {
    public ApiResponseAdapter(DaddySerializer daddySerializer) {
        super(daddySerializer);
    }

    @Override
    public void serialize(ApiResponse<T> obj, GrowableBuffer buf) {
        obj.on(
                data -> {
                    buf.putByte((byte) 0x00);
                    daddySerializer.serialize(data, null, buf, false, new boolean[0]);
                },
                error -> {
                    buf.putByte((byte) 0x01);
                    daddySerializer.serialize(error, null, buf, false, new boolean[0]);
                }
        );
    }

    @Override
    public ApiResponse<T> deserialize(ByteBuffer data, TypeContainer<ApiResponse<T>> type) throws SerializationException {
        byte success = data.get();
        if (success == (byte) 0x00) {
            @SuppressWarnings("unchecked")
            TypeContainer<T> subType = (TypeContainer<T>) type.getGenericArgument(0);
            T successData = daddySerializer.deserialize(data, subType, false, new boolean[0]);
            return ApiResponse.success(successData);
        } else if (success == (byte) 0x01) {
            String errorMessage = daddySerializer.deserialize(data, new TypeContainer<>(String.class), false, new boolean[0]);
            return ApiResponse.error(errorMessage);
        } else {
            throw new SerializationException("Invalid ApiResponse type: " + type);
        }
    }
}
