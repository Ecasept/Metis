package dev.ecasept.unitodo.models.serialization.adapters;

import dev.ecasept.unitodo.models.serialization.GrowableBuffer;
import dev.ecasept.unitodo.models.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.models.serialization.serializers.StringSerializer;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter extends Adapter<LocalDateTime> {
        private static final String TAG = "LocalDateTimeAdapter";
        private static final StringSerializer stringSerializer = new StringSerializer();
        public LocalDateTimeAdapter(DaddySerializer daddySerializer) {
                super(daddySerializer);
        }

        public void serialize(LocalDateTime obj, GrowableBuffer buf) {
                String s = obj.toString();
                stringSerializer.serialize(s, buf);

        }
        public LocalDateTime deserialize(ByteBuffer data) {
                String s = stringSerializer.deserialize(data);
                return LocalDateTime.parse(s);
        }
}
