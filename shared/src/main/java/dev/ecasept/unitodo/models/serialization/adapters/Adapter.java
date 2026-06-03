package dev.ecasept.unitodo.models.serialization.adapters;

import dev.ecasept.unitodo.models.serialization.GrowableBuffer;
import dev.ecasept.unitodo.models.serialization.serializers.BaseSerializer;
import dev.ecasept.unitodo.models.serialization.serializers.DaddySerializer;

import java.nio.ByteBuffer;

public abstract class Adapter<T> extends BaseSerializer {
        protected DaddySerializer daddySerializer;
        public Adapter(DaddySerializer daddySerializer) {
                this.daddySerializer = daddySerializer;
        }

        public abstract void serialize(T obj, GrowableBuffer buf);
        public abstract T deserialize(ByteBuffer data);
}
