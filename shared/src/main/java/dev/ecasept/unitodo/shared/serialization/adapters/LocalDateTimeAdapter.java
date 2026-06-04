package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.shared.serialization.serializers.StringSerializer;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends Adapter<LocalDateTime> {
        private static final String TAG = "LocalDateTimeAdapter";
        private static final StringSerializer stringSerializer = new StringSerializer();
        public LocalDateTimeAdapter(DaddySerializer daddySerializer) {
                super(daddySerializer);
        }

        public void serialize(LocalDateTime obj, GrowableBuffer buf) {
                Log.i(TAG, "Serializing LocalDateTime: " + obj.toString());
                String s = obj.toString();
                stringSerializer.serialize(s, buf);

        }
        public LocalDateTime deserialize(ByteBuffer data, TypeContainer<LocalDateTime> type) throws SerializationException {
                String s = stringSerializer.deserialize(data);
                Log.i(TAG, "Deserialized LocalDateTime string: " + s);
                return LocalDateTime.parse(s);
        }
}
