package dev.ecasept.unitodo.shared.serialization.adapters;

import dev.ecasept.unitodo.shared.serialization.compilers.NullableTypeContainer;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;

/**
 * Adapter that specifies how to compile and (de-)serialize specific types.
 * @param <T> The type that this adapter can handle
 */
public interface Adapter<T> {
    /**
     * Creates a schema for the specified type that implements the (de-)serialization for the type.
     * @param nullableType The type that should be converted to a schema
     * @return A schema that knows how to (de-)serialize objects of type {@link T}
     */
    Schema<T> compileToSchema(NullableTypeContainer<T> nullableType);
}
