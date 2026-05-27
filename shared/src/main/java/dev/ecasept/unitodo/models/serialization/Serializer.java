package dev.ecasept.unitodo.models.serialization;

import dev.ecasept.unitodo.models.serialization.adapters.Adapter;
import dev.ecasept.unitodo.models.serialization.serializers.DaddySerializer;
import dev.ecasept.unitodo.models.serialization.serializers.ObjectSerializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

public class Serializer {
    private boolean rootNullable = false;
    private boolean[] rootNullableElements = {};
    private ArrayList<Adapter<?>> adapters = new ArrayList<>();

    public Serializer setRootNullable(boolean rootNullable) {
        this.rootNullable = rootNullable;
        return this;
    }
    public Serializer addAdapters(Adapter<?>[] adapters) {
        this.adapters.addAll(Arrays.stream(adapters).toList());
        return this;
    }

    public Serializer setRootNullableElements(boolean[] rootNullableElements) {
        this.rootNullableElements = rootNullableElements;
        return this;
    }

    public byte[] serialize(Object o) {
        var daddySerializer = new DaddySerializer(adapters);
        var buf = new GrowableBuffer(256, ByteOrder.BIG_ENDIAN);
        daddySerializer.serialize(o, null, buf, rootNullable, rootNullableElements);
        return buf.toBytes();
    }
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        var daddySerializer = new DaddySerializer(adapters);
        var buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        return daddySerializer.deserialize(buf, clazz, rootNullable, rootNullableElements);
    }
}
