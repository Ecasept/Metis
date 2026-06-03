package dev.ecasept.unitodo.shared.serialization;

import dev.ecasept.unitodo.shared.serialization.adapters.Adapter;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;import dev.ecasept.unitodo.shared.serialization.types.StoreType;import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;


/**
 * Custom binary serializer
 */
public class Serializer {
    private boolean rootNullable = false;
    private boolean[] rootNullableElements = {};
    private final HashMap<Class<?>, Class<? extends Adapter<?>>> adapters = new HashMap<>();

    /**
     * Determines whether the root object, i.e. the object that is being serialized itself, can be null itself.
     * <br><br>
     * What is allowed/can happen when the root object is null:
     * <pre>
     * s.serialize(null); // no errors
     * var out = s.deserialize(bytes, SomeClass.class); // `out` could be null
     * </pre>
     * @param rootNullable Whether the root is nullable or not
     * @return <code>this</code>
     */
    public Serializer setRootNullable(boolean rootNullable) {
        this.rootNullable = rootNullable;
        return this;
    }
    public <T> Serializer adapter(Class<? extends Adapter<T>> adapter, Class<T> clazz) {
        this.adapters.put(clazz, adapter);
        return this;
    }

    /**
     * Works just like Field.nulllableElements, but for the root object
     */
    public Serializer setRootNullableElements(boolean[] rootNullableElements) {
        this.rootNullableElements = rootNullableElements;
        return this;
    }

    /**
     * Converts an object that belongs to a serializable class into a binary recoverable representation of it.
     * @param o The object to be serialized
     * @return The serialized bytes
     */
    public byte[] serialize(Object o) {
        var daddySerializer = new DaddySerializer(adapters);
        var buf = new GrowableBuffer(256, ByteOrder.BIG_ENDIAN);
        daddySerializer.serialize(o, null, buf, rootNullable, rootNullableElements);
        return buf.toBytes();
    }
    /**
     * Tries to convert a binary representation of an object into its deserialized form.
     * @param data The binary representation that should be deserialized
     * @param clazz The target class that the data should be deserialized into.
     * @return The deserialized object as the correct class
     */
    public <T, R extends StoreType<T>> T deserialize(byte[] data, R type) {
        var daddySerializer = new DaddySerializer(adapters);
        var buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        return daddySerializer.deserialize(buf, new TypeContainer<>(type), rootNullable, rootNullableElements);
    }
}
