package dev.ecasept.unitodo.shared.serialization;

import dev.ecasept.unitodo.shared.serialization.adapters.*;
import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.serializers.DaddySerializer;import dev.ecasept.unitodo.shared.serialization.types.StoreType;import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;


/**
 * Custom binary serializer
 * <p>
 * The serializer features two main methods: {@link Serializer#serialize} and {@link Serializer#deserialize}.
 * {@link Serializer#serialize} takes an object and converts it into a byte array, while {@link Serializer#deserialize} takes a byte array and a target class and tries to convert the byte array back into an object of the target class.
 * <p>
 * The serializer assumes the serialization/deserialization schema is known (and provided for deserialization),
 * and therefore the specific object types won't be encoded into the byte array. This results in a more efficient
 * serialization format.
 * <p>
 * The serializer also supports custom adapters, which can be registered for specific classes.
 * An adapter can define custom serialization/deserialization logic for its target class.
 * This is useful for classes that require special serialization logic (e.g. classes that have fields that should not be serialized),
 * or classes not under your control that cannot be annotated for automatic serialization.
 */
public class Serializer {
    private boolean rootNullable = false;
    private boolean[] rootNullableElements = {};
    private final HashMap<Class<?>, Class<? extends Adapter<?>>> adapters = new HashMap<>();

    /**
     * Determines whether the root object, i.e. the object that is being serialized itself, can be null itself.
     * <p>
     * What is allowed/can happen when the root object is null:
     * <pre>{@code
     * s.serialize(null); // no errors
     * var out = s.deserialize(bytes, SomeClass.class); // `out` could be null
     * }</pre>
     * @param rootNullable Whether the root is nullable or not
     * @return {@code this}
     */
    public Serializer setRootNullable(boolean rootNullable) {
        this.rootNullable = rootNullable;
        return this;
    }

    /**
     * Registers an adapter for a specific class. The adapter will be used to serialize and deserialize objects of the specified class.
     * @param adapter The class of the adapter to register
     * @param clazz The class that the adapter should be used for
     * @return {@code this}
     * @param <T> The actual type of the class that the adapter is used for.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Serializer adapter(Class<? extends Adapter> adapter, Class<T> clazz) {
        this.adapters.put(clazz, (Class<? extends Adapter<?>>) adapter);
        return this;
    }

    /**
     * Works just like {@link Field#nullableElements()}, but for the root object
     */
    public Serializer setRootNullableElements(boolean[] rootNullableElements) {
        this.rootNullableElements = rootNullableElements;
        return this;
    }

    /**
     * Converts an object that belongs to a serializable class into a recoverable binary representation of it.
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
     * @param type The type of the target class that the data should be deserialized into.
     *             This needs to be passed using {@link StoreType} to preserve generic information
     *             that would usually be lost through type erasure.
     * @return The deserialized object as the correct class
     * @param <T> The type of the deserialized object
     * @param <R> The type of the StoreType wrapper (which is usually a subclass of {@link StoreType}) around the deserialized object
     */
    public <T, R extends StoreType<T>> T deserialize(byte[] data, R type) throws SerializationException {
        var daddySerializer = new DaddySerializer(adapters);
        var buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        return daddySerializer.deserialize(buf, new TypeContainer<>(type), rootNullable, rootNullableElements);
    }

    public static Serializer createDefault() {
        return new Serializer().adapter(RawDataAdapter.class, RawData.class).adapter(LocalDateTimeAdapter.class, LocalDateTime.class).adapter(OptionalAdapter.class, Optional.class).adapter(UUIDAdapter.class, UUID.class);
    }
}
