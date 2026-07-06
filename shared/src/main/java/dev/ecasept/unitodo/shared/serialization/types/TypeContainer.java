package dev.ecasept.unitodo.shared.serialization.types;

import java.lang.reflect.*;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * A class that wraps a {@link java.lang.reflect.Type} and provides useful methods for working on it.
 * @param <T> The type that this container represents. This is only used for type safety and does not affect the runtime behavior of the class.
 */
public class TypeContainer<T> {
    private final Type type;

    private TypeContainer(Type type) {
        this.type = type;
    }

    /**
     * Creates a new {@code TypeContainer} with the type provided by the given {@link StoreType}.
     * @param storeType The stored type to wrap.
     * @param <T> The type to store. Ensures that {@code storeType} stores the type {@code <T>}. This is only used for type safety and does not affect the runtime behavior of the class.
     */
    public static <T> TypeContainer<T> of(StoreType<T> storeType) {
        return TypeContainer.of(storeType.getType());
    }

    /**
     * Creates a new {@code TypeContainer} with the given type.
     * @param type The type to wrap. This can be any implementation of {@link java.lang.reflect.Type}, such as {@link Class}, {@link ParameterizedType}, {@link GenericArrayType}, {@link TypeVariable}, or {@link WildcardType}.
     */
    public static <T> TypeContainer<T> of(Type type) {
        return new TypeContainer<>(type);
    }

    @Override
    public boolean equals(Object otherContainer) {
        if (this == otherContainer) {
            return true;
        } else if (otherContainer instanceof TypeContainer<?> other) {
            return equals(other, self -> false);
        }
        return false;
    }

    /** Checks whether two {@code TypeContainer}s are equal. You can add a function that can add special behavior for special types, like {@link dev.ecasept.unitodo.shared.serialization.adapters.Any}.
     * @param otherContainer The other {@code TypeContainer} to compare to.
     * @param additionalComparison A function that will be called on the current {@link TypeContainer} {@code this} (or parts of it, e.g. if {@code this} is {@code List<String>}, it might also be called on {@code String}) that can short circuit the comparison by saying that this (sub-)type is equal to anything if it returns {@code true}. Returning {@code false} makes the function fallback to the default behavior.
     * @return Whether the types are equal.
     */
    public boolean equals(TypeContainer<?> otherContainer, Function<TypeContainer<?>, Boolean> additionalComparison) {
        if (otherContainer == null) {
            // don't throw NPE
            return false;
        }
        var other = otherContainer.type;
        if (additionalComparison.apply(this)) {
            return true;
        }
        switch (type) {
            case null -> {
                return other == null;
            }
            case Class<?> ca when other instanceof Class<?> cb -> {
                return ca.equals(cb);
            }
            case ParameterizedType pa when other instanceof ParameterizedType pb -> {
                var rawA = pa.getRawType();
                var rawB = pb.getRawType();
                if (!TypeContainer.of(rawA).equals(TypeContainer.of(rawB), additionalComparison)) {
                    return false;
                }
                var argsA = pa.getActualTypeArguments();
                var argsB = pb.getActualTypeArguments();
                if (argsA.length != argsB.length) {
                    return false;
                }
                for (int i = 0; i < argsA.length; i++) {
                    if (!TypeContainer.of(argsA[i]).equals(TypeContainer.of(argsB[i]), additionalComparison)) {
                        return false;
                    }
                }
                var ownerA = pa.getOwnerType();
                var ownerB = pb.getOwnerType();
                return ownerA == null ? ownerB == null : TypeContainer.of(ownerA).equals(TypeContainer.of(ownerB), additionalComparison);
            }
            case GenericArrayType ga when other instanceof GenericArrayType gb -> {
                return TypeContainer.of(ga.getGenericComponentType()).equals(TypeContainer.of(gb.getGenericComponentType()), additionalComparison);
            }
            default -> {
                // TypeVariables and WildcardTypes are not supported and should not be contained inside a TypeContainer
                return false;
            }
        }
    }

    @Override
    public int hashCode() {
        return switch (type) {
            case null -> 0;
            case Class<?> c -> c.hashCode();
            case ParameterizedType pt -> {
                int result = TypeContainer.of(pt.getRawType()).hashCode();
                for (Type arg : pt.getActualTypeArguments()) {
                    result = 31 * result + TypeContainer.of(arg).hashCode();
                }
                Type owner = pt.getOwnerType();
                result = 31 * result + (owner == null ? 0 : TypeContainer.of(owner).hashCode());
                yield result;
            }
            case GenericArrayType gat -> 31 * TypeContainer.of(gat.getGenericComponentType()).hashCode();
            default -> type.hashCode();
        };
    }

    /**
     * Casts the given object to the type represented by this container. This is an unchecked cast and may throw a {@link ClassCastException} at runtime if the object is not actually of the correct type. Use with caution.
     * <p>
     * Example:
     * <pre>{@code
     * public <T> void doSomething(TypeContainer<T> tc, T obj) {
     *    // do something with tc and obj
     *
     *    // We want to return a String if T is String, otherwise we just return the object as is.
     *    if (tc.isString()) {
     *        // We now know that T is String, so we can safely cast "Hello, World!" to T and return it.
     *        return tc.cast("Hello, World!");
     *    } else {
     *        return obj;
     *    }
     * }
     * }</pre>
     *
     * @param obj The object to cast. This should be an instance of the type represented by this container, but this is not enforced at compile time.
     * @return The given object cast to the type represented by this container.
     */
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        return (T) obj;
    }

    /**
     * Checks whether the type represented by this container is {@code void} or {@link Void}.
     * @return {@code true} if the type is {@code void} or {@link Void}, {@code false} otherwise.
     */
    public boolean isVoid() {
        if (type instanceof Class<?> clazz) {
            return clazz == Void.class || clazz == void.class;
        }
        return false;
    }

    /**
     * Checks whether the type represented by this container is a primitive type (e.g. {@code int}, {@code boolean}, etc.). Note that wrapper types (e.g. {@link Integer}, {@link Boolean}, etc.) are not considered primitive.
     * @return {@code true} if the type is a primitive type, {@code false} otherwise.
     */
    public boolean isPrimitive() {
        if (type instanceof Class<?> clazz) {
            return clazz.isPrimitive();
        }
        return false;
    }

    /**
     * Returns the type represented by this container as a primitive {@code Class<?>}.
     * @return The type as a primitive.
     * @throws IllegalArgumentException When the underlying type is not actually a primitive. Please check first using {@link TypeContainer#isPrimitive()}.
     */
    public Class<?> asPrimitive() {
        if (type instanceof Class<?> clazz) {
            if (clazz.isPrimitive()) {
                return clazz;
            }
        }
        throw new IllegalArgumentException("Type is not a primitive: " + type);
    }

    /**
     * Checks whether the type represented by this container is an array type,
     * for example a {@link GenericArrayType} like {@code List<String>[]}, a normal array like {@code String[]}, or a primitive array like {@code int[]}.
     * @return {@code true} if the type is an array type, {@code false} otherwise.
     */
    public boolean isArray() {
        if (type instanceof Class<?> clazz) {
            return clazz.isArray();
        } else return type instanceof GenericArrayType;
    }

    /**
     * Checks whether the type represented by this container is a {@link GenericArrayType},
     * for example {@code List<String>[]}.
     * @return {@code true} if the type is a {@link GenericArrayType}, {@code false} otherwise.
     */
    public boolean isGenericArray() {
        return type instanceof GenericArrayType;
    }

    /**
     * Returns the component type of the underlying array.
     * <p>
     * Example:
     * <br>
     * {@code List<String>[]} will return {@code List<String>}.
     *
     * @return The component type.
     * @throws IllegalArgumentException When the underlying type is not actually an array. Please check first using {@link TypeContainer#isArray()}.
     */
    public Type getComponentType() {
        if (type instanceof Class<?> clazz) {
            if (clazz.isArray()) {
                return clazz.getComponentType();
            }
        } else if (type instanceof GenericArrayType arrayType) {
            return arrayType.getGenericComponentType();
        }
        throw new IllegalArgumentException("Type is not an array: " + type);
    }

    /**
     * Checks whether the type represented by this container is a class.
     * @return {@code true} if the type is a class, {@code false} otherwise.
     */
    public boolean isClass() {
        return type instanceof Class<?>;
    }

    /**
     * Returns the type represented by this container as a {@code Class<?>}.
     * @return The type as a class.
     * @throws IllegalArgumentException When the underlying type is not actually a class. Please check first using {@link TypeContainer#isClass()}.
     */
    public Class<?> asClass() {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        throw new IllegalArgumentException("Type is not a class: " + type);
    }

    /**
     * Returns the first level class of the type represented by this container.
     * If the type is a class, it returns that class.
     * If the type is a parameterized type, it returns the raw type of that parameterized type.
     * For example, if the type is {@code List<String>}, this method will return {@link java.util.List}.
     * @return The raw class represented as a {@code Class<?>}.
     * @throws IllegalArgumentException When the underlying type is not actually a {@link Class} or a {@link ParameterizedType}.
     */
    public Class<?> getRawClass() {
        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType paramType) {
            return (Class<?>) paramType.getRawType(); // This cast is safe because the raw type of a parameterized type is always a class
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    /**
     * Returns the first level class just like {@link TypeContainer#getRawClass},
     * but if the type is a primitive, it will return the corresponding wrapper class instead.
     * For example, if the type is {@code int}, this method will return {@link Integer}.
     * @return The raw class, with primitives converted to their wrappers
     */
    public Class<?> getRawWrapperClass() {
        var clazz = getRawClass();
        if (clazz == int.class)     return Integer.class;
        if (clazz == boolean.class) return Boolean.class;
        if (clazz == double.class)  return Double.class;
        if (clazz == long.class)    return Long.class;
        if (clazz == char.class)    return Character.class;
        if (clazz == byte.class)    return Byte.class;
        if (clazz == short.class)   return Short.class;
        if (clazz == float.class)   return Float.class;
        return clazz;
    }

    /**
     * Checks whether the type represented by this container is a wildcard type (e.g. {@code ?}, {@code ? extends Number}, etc.).
     * @return {@code true} if the type is a wildcard type, {@code false} otherwise.
     */
    public boolean isWildcard() {
        return type instanceof WildcardType;
    }

    /**
     * Checks whether the type represented by this container is a type variable (e.g. {@code T} in {@code class ApiResponse<T>}).
     * @return {@code true} if the type is a type variable, {@code false} otherwise.
     */
    public boolean isTypeVariable() {
        return type instanceof TypeVariable;
    }

    /**
     * Checks whether the type represented by this container is a parameterized type (e.g. {@code List<String>}, {@code Map<String, Integer>}, etc.).
     * @return {@code true} if the type is a parameterized type, {@code false} otherwise.
     */
    public boolean isParameterized() {
        return type instanceof ParameterizedType;
    }

    /**
     * Checks whether the type represented by this container is {@link String}.
     * @return {@code true} if the type is {@link String}, {@code false} otherwise.
     */
    public boolean isString() {
        if (type instanceof Class<?> clazz) {
            return clazz == String.class;
        }
        return false;
    }

    /**
     * Checks whether the type represented by this container is a wrapper type for a primitive (e.g. {@link Integer} for {@code int}, {@link Boolean} for {@code boolean}, etc.). Primitive types themselves (e.g. {@code int}, {@code boolean}, etc.) are not considered wrapper types.
     * @return {@code true} if the type is a wrapper type for a primitive, {@code false} otherwise.
     */
    public boolean isWrapper() {
        if (type instanceof Class<?> clazz) {
            return clazz == Boolean.class || clazz == Byte.class || clazz == Character.class ||
                   clazz == Short.class || clazz == Integer.class || clazz == Long.class ||
                   clazz == Float.class || clazz == Double.class;
        }
        return false;
    }

    /**
     * Wrapper around {@link Type#getTypeName()} for convenience.
     * @return The name of the type represented by this container.
     */
    public String getTypeName() {
        return type.getTypeName();
    }

    /**
     * Tries to smartly resolve the type of the given field on the type represented by this container.
     * This is especially useful for resolving the types of fields that are declared using type variables (e.g. {@code T} in {@code class ApiResponse<T>}),
     * as it will try to resolve those type variables using the actual type arguments provided to this container
     * (e.g. if this container represents {@code ApiResponse<String>}, it will resolve {@code T} to {@link String}).
     * <p>
     * Note: This method should only be called for fields that are fetched through {@link Class#getDeclaredFields()} (which does not return fields of superclasses) as this method does not look at the superclass when resolving type variables.
     * @param field The field whose type should be resolved. This field should be declared in the class represented by this container.
     * @return A new {@code TypeContainer} representing the type of the given field, with type variables resolved if possible.
     * @throws IllegalArgumentException When the field's type cannot be accurately resolved (e.g. if it's a type variable that cannot be resolved, or a wildcard).
     */
    public TypeContainer<?> getFieldType(Field field) {
        return resolveType(field.getGenericType());
    }

    /**
     * Does the same thing as {@link TypeContainer#getFieldType(Field)}, but for a {@link RecordComponent}
     */
    public TypeContainer<?> getRecordComponentType(RecordComponent recordComponent) {
        return resolveType(recordComponent.getGenericType());
    }
    private TypeContainer<?> resolveType(Type fieldType) {
        switch (fieldType) {
            case Class<?> clazz -> {
                return TypeContainer.of(clazz);
            }
            case ParameterizedType paramType -> {
                var isDifferent = false;
                var typeArgs = paramType.getActualTypeArguments();
                var resolvedTypes = new TypeContainer<?>[typeArgs.length];
                for (int i = 0; i < typeArgs.length; i++) {
                    var resolvedType = resolveType(typeArgs[i]);
                    if (!TypeContainer.of(typeArgs[i]).equals(resolvedType)) {
                        isDifferent = true;
                    }
                    resolvedTypes[i] = resolvedType;
                }
                var resolvedOwnerType = paramType.getOwnerType() != null ? resolveType(paramType.getOwnerType()) : null;
                if (resolvedOwnerType != null && resolvedOwnerType.equals(TypeContainer.of(paramType.getOwnerType()))) {
                    isDifferent = true;
                }

                if (!isDifferent) {
                    return TypeContainer.of(paramType);
                } else {
                    // Create a new ParameterizedType with the resolved type arguments
                    var rawType = (Class<?>) paramType.getRawType();
                    return TypeContainer.of(new ParameterizedType() {
                        @SuppressWarnings("NullableProblems")
                        @Override
                        public Type[] getActualTypeArguments() {
                            Type[] resolvedTypeArgs = new Type[resolvedTypes.length];
                            for (int i = 0; i < resolvedTypes.length; i++) {
                                resolvedTypeArgs[i] = resolvedTypes[i].type;
                            }
                            return resolvedTypeArgs;
                        }

                        @SuppressWarnings("NullableProblems")
                        @Override
                        public Type getRawType() {
                            return rawType;
                        }

                        @Override
                        public Type getOwnerType() {
                            return resolvedOwnerType != null ? resolvedOwnerType.type : null;
                        }

                        /** Stolen from {@code ParameterizedTypeImpl.toString()} */
                        @Override
                        public String toString() {
                            StringBuilder sb = new StringBuilder();
                            if (getOwnerType() != null) {
                                sb.append(getOwnerType().getTypeName());

                                sb.append("$");

                                if (getOwnerType() instanceof ParameterizedType ownerType) {
                                    sb.append((rawType.getName().replace(((Class<?>)ownerType.getRawType()).getName() + "$", "")));
                                } else
                                    sb.append(rawType.getSimpleName());
                            } else
                                sb.append(rawType.getName());

                            StringJoiner sj = new StringJoiner(", ", "<", ">");
                            sj.setEmptyValue("");
                            for(Type t : getActualTypeArguments()) {
                                sj.add(t.getTypeName());
                            }
                            sb.append(sj);

                            return sb.toString();
                        }
                    });
                }
            }
            case GenericArrayType arrayType -> {
                var componentType = arrayType.getGenericComponentType();
                var resolvedComponentType = resolveType(componentType);
                if (TypeContainer.of(componentType).equals(resolvedComponentType)) {
                    return TypeContainer.of(arrayType);
                } else {
                    // Create a new GenericArrayType with the resolved component type
                    return TypeContainer.of(new GenericArrayType() {
                        @SuppressWarnings("NullableProblems")
                        @Override
                        public Type getGenericComponentType() {
                            return resolvedComponentType.type;
                        }

                        @Override
                        public String toString() {
                            return resolvedComponentType.getTypeName() + "[]";
                        }
                    });
                }
            }
            case TypeVariable<?> typeVar -> {
                // Try to resolve the type variable using the class's type parameters
                if (type instanceof ParameterizedType paramType) {
                    // The generic types that this class takes (e.g. T in ApiResponse<T>)
                    TypeVariable<?>[] typeParams = ((Class<?>) paramType.getRawType()).getTypeParameters();
                    // The actual types that were used for this class (e.g. String in ApiResponse<String>)
                    Type[] typeArgs = paramType.getActualTypeArguments();

                    for (int i = 0; i < typeParams.length; i++) {
                        if (typeParams[i].equals(typeVar)) {
                            return TypeContainer.of(typeArgs[i]);
                        }
                    }
                }
                throw new IllegalArgumentException("Cannot resolve type variable: " + typeVar);
            }
            default -> throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }

    /**
     * Returns the generic type argument at the given index if the underlying type is a parameterized type.
     * <p>
     * Example:
     * If the underlying type is {@code Map<String, Integer>}, then {@code getGenericArgument(0)} will return a {@code TypeContainer} representing {@link String}, and {@code getGenericArgument(1)} will return a {@code TypeContainer} representing {@link Integer}.
     * @param index The index of the generic argument to return. This should be a non-negative integer less than the number of generic arguments of the underlying parameterized type.
     * @return A new {@code TypeContainer} representing the generic argument at the given index.
     * @throws IllegalArgumentException When the underlying type is not a parameterized type (you can check first using {@link TypeContainer#isParameterized()}), or when the index is out of bounds.
     */
    public TypeContainer<?> getGenericArgument(int index) {
        if (type instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (index < typeArgs.length) {
                return new TypeContainer<>(typeArgs[index]);
            } else {
                throw new IllegalArgumentException("Index out of bounds: " + index + " for type " + type.getTypeName());
            }
        }
        throw new IllegalArgumentException("Type " + type.getTypeName() + " is not parameterized");
    }
}
