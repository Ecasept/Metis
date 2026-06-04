package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.serializers.BaseSerializer;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;

public abstract class Adapter<T> extends BaseSerializer {
        protected final DaddySerializer daddySerializer;
        public Adapter(DaddySerializer daddySerializer) {
                this.daddySerializer = daddySerializer;
        }

        public abstract void serialize(T obj, GrowableBuffer buf);
        public abstract T deserialize(ByteBuffer data, TypeContainer<T> type) throws SerializationException;
}
