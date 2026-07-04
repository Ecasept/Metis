package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;

public class RawDataAdapter implements Adapter<RawData> {
    private static final String TAG = "RawDataAdapter";

    @Override
    public Schema<RawData> compileToSchema(NullableTypeContainer<RawData> nullableType) {
        return new Schema<>() {
            @Override
            public void serialize(RawData obj, GrowableBuffer buf) {
                Log.i(TAG, "Serializing RawData of length: " + obj.data().length);
                buf.putBytes(obj.data());
            }

            @Override
            public RawData deserialize(ByteBuffer data) {
                Log.i(TAG, "Deserializing RawData of length: " + data.remaining());
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                return new RawData(bytes);
            }
        };
    }
}
