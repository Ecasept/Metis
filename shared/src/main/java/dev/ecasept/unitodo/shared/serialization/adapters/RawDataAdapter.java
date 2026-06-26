package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;

public class RawDataAdapter extends Adapter<RawData> {
    private static final String TAG = "RawDataAdapter";
    public RawDataAdapter(DaddySerializer daddySerializer) {
        super(daddySerializer);
    }

    @Override
    public void serialize(RawData obj, GrowableBuffer buf) {
        Log.i(TAG, "Serializing RawData of length: " + obj.data().length);
        buf.putBytes(obj.data());
    }

    @Override
    public RawData deserialize(ByteBuffer data, TypeContainer<RawData> type) {
        Log.i(TAG, "Deserializing RawData of length: " + data.remaining());
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        return new RawData(bytes);
    }
}
