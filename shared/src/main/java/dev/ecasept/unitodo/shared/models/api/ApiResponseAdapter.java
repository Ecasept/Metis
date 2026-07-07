package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.adapters.Adapter;
import dev.ecasept.unitodo.shared.serialization.compilers.DaddyCompiler;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;

public class ApiResponseAdapter<T> implements Adapter<ApiResponse<T>> {
    private final DaddyCompiler daddyCompiler;

    public ApiResponseAdapter(DaddyCompiler daddyCompiler) {
        this.daddyCompiler = daddyCompiler;
    }

    @Override
    public Schema<ApiResponse<T>> compileToSchema(NullableTypeContainer<ApiResponse<T>> nullableType) {
        //noinspection unchecked
        var dataType = (TypeContainer<T>) nullableType.type().getGenericArgument(0);
        var dataSchema = daddyCompiler.compileToSchema(NullableTypeContainer.of(dataType, false));
        var errorCodeSchema = daddyCompiler.compileToSchema(NullableTypeContainer.of(ErrorCode.class, false));
        var stringSchema = daddyCompiler.compileToSchema(NullableTypeContainer.of(String.class, false));
        return new Schema<>() {
            @Override
            public void serialize(ApiResponse<T> obj, GrowableBuffer buf) {
                obj.on(
                        data -> {
                            buf.putByte((byte) 0x00);
                            dataSchema.serialize(data, buf);
                        },
                        (msg, code) -> {
                            buf.putByte((byte) 0x01);
                            stringSchema.serialize(msg, buf);
                            errorCodeSchema.serialize(code, buf);
                        }
                );
            }

            @Override
            public ApiResponse<T> deserialize(ByteBuffer data) throws SerializationException {
                byte success = data.get();
                if (success == (byte) 0x00) {
                    T successData = dataSchema.deserialize(data);
                    return ApiResponse.success(successData);
                } else if (success == (byte) 0x01) {
                    String errorMessage = stringSchema.deserialize(data);
                    ErrorCode errorCode = errorCodeSchema.deserialize(data);
                    return ApiResponse.error(errorMessage, errorCode);
                } else {
                    throw new SerializationException("Invalid ApiResponse tag: " + String.format("0x%02X", success));
                }
            }
        };
    }
}
