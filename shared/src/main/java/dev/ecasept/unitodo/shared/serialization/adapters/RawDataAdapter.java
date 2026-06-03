package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;

import java.nio.ByteBuffer;

public class RawDataAdapter extends Adapter<RawData> {
        private static final String TAG = "RawDataAdapter";
        public RawDataAdapter(DaddySerializer daddySerializer) {
            super(daddySerializer);
        }

        public void serialize(RawData obj, GrowableBuffer buf) {
            buf.putBytes(obj.data());
        }
        public RawData deserialize(ByteBuffer data) {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            return new RawData(bytes);
        }
}
