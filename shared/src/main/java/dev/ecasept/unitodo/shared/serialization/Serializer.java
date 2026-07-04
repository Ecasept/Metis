package dev.ecasept.unitodo.shared.serialization;

import dev.ecasept.unitodo.shared.serialization.adapters.*;
import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.compilers.DaddyCompiler;
import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;


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
    private final HashMap<TypeContainer<?>, Function<DaddyCompiler, Adapter<?>>> adapters = new HashMap<>();
    private DaddyCompiler daddyCompiler;

    /**
     * Determines whether the root object, i.e. the object that is being serialized itself, can be null itself.
     * <p>
     * What is allowed/can happen when the root object is null:
     * <pre>{@code
     * s.serialize(null); // no errors
     * var out = s.deserialize(bytes, SomeClass.class); // `out` could be null
     * }</pre>
     * @param rootNullable Whether the root is nullableType or not
     * @return {@code this}
     */
    public Serializer setRootNullable(boolean rootNullable) {
        this.rootNullable = rootNullable;
        return this;
    }

    /**
     * Registers an adapter for a specific class. The adapter will be used to serialize and deserialize objects of the specified class.
     * @param adapter A function returning the instance of the adapter to use. Receives a {@link DaddyCompiler} as an argument, which can be used to create schemas inside the adapter. Might be called multiple times.
     * @param type The type that the adapter should be used for. You can use {@link Any} to allow the adapter to be used for multiple types, e.g. {@code Optional<Any>}.
     * @return {@code this}
     * @param <T> The actual type of the class that the adapter is used for.
     */
    public <T> Serializer adapter(Function<DaddyCompiler, Adapter<T>> adapter, StoreType<T> type) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        var casted = (Function<DaddyCompiler, Adapter<?>>) (Function) adapter;
        this.adapters.put(TypeContainer.of(type), casted);
        this.daddyCompiler = null;
        return this;
    }

    /** Wrapper for {@link Serializer#adapter(Function, StoreType)} for when you don't need the {@link DaddyCompiler} in your adapter. */
    public <T> Serializer adapter(Supplier<Adapter<T>> adapter, StoreType<T> type) {
        return adapter(daddySchemaCreator -> adapter.get(), type);
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
     * @param type The type of the object that is being serialized, stored inside a {@link StoreType} to preserve generic information at runtime.
     * @return The serialized bytes
     * @param <T> The type of the object that is being serialized
     */
    public <T> byte[] serialize(T o, StoreType<T> type) {
        if (daddyCompiler == null) {
            daddyCompiler = new DaddyCompiler(adapters);
        }
        var buf = new GrowableBuffer(256, ByteOrder.BIG_ENDIAN);
        var schema = daddyCompiler.compileToSchema(NullableTypeContainer.of(type.asTypeContainer(), rootNullable, rootNullableElements));
        schema.serialize(o, buf);
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
     */
    public <T> T deserialize(byte[] data, StoreType<T> type) throws SerializationException {
        if (daddyCompiler == null) {
            daddyCompiler = new DaddyCompiler(adapters);
        }
        var buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        var schema = daddyCompiler.compileToSchema(NullableTypeContainer.of(type.asTypeContainer(), rootNullable, rootNullableElements));
        return schema.deserialize(buf);
    }

    /** Returns a new serializer with a few built-in adapters for common types. */
    public static Serializer createDefault() {
        return new Serializer()
                .adapter(RawDataAdapter::new, new StoreType<>() {})
                .adapter(d -> new TemporalAdapter<>(d, LocalDateTime::from), new StoreType<>(){})
                .adapter(d -> new TemporalAdapter<>(d, DateTimeFormatter.ISO_DATE, LocalDate::from), new StoreType<>(){})
                .adapter(d -> new TemporalAdapter<>(d, DateTimeFormatter.ISO_TIME, LocalTime::from), new StoreType<>(){})
                .adapter(OptionalAdapter<Any>::new, new StoreType<>() {})
                .adapter(UUIDAdapter::new, new StoreType<>() {});
    }
}
