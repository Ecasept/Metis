package dev.ecasept.unitodo.shared.serialization.compilers;

import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.util.Arrays;

/**
 * Represents a type and its associated nullability metadata
 * @param type The type stored as a concrete Java object
 * @param nullable Whether the objects of this type are allowed to be {@code null}
 * @param nullableElements (Only for arrays) specified the nullability for each dimension of the array
 * @param arrDim Specifies the current dimension of the array that the current type is at.
 * @param <T> The actual type on the type-system level
 */
public record NullableTypeContainer<T>(TypeContainer<T> type, boolean nullable, boolean[] nullableElements,
                                       int arrDim) {

    public static <T> NullableTypeContainer<T> of(TypeContainer<T> type, boolean nullable) {
        return new NullableTypeContainer<>(type, nullable, new boolean[0], 0);
    }
    public static <T> NullableTypeContainer<T> of(TypeContainer<T> type, boolean nullable, boolean[] nullableElements) {
        return new NullableTypeContainer<>(type, nullable, nullableElements, 0);
    }
    public static <T> NullableTypeContainer<T> of(Class<T> clazz, boolean nullable) {
        return new NullableTypeContainer<>(TypeContainer.of(clazz), nullable, new boolean[0], 0);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NullableTypeContainer<?> other = (NullableTypeContainer<?>) o;
        return nullable == other.nullable && arrDim == other.arrDim && type.equals(other.type) && Arrays.equals(nullableElements, other.nullableElements);
    }
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + Boolean.hashCode(nullable);
        result = 31 * result + Arrays.hashCode(nullableElements);
        result = 31 * result + arrDim;
        return result;
    }
}
