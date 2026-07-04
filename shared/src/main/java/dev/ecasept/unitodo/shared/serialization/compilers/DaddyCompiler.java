package dev.ecasept.unitodo.shared.serialization.compilers;

import dev.ecasept.unitodo.shared.serialization.adapters.Adapter;
import dev.ecasept.unitodo.shared.serialization.adapters.Any;
import dev.ecasept.unitodo.shared.serialization.schemas.*;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DaddyCompiler {
    private static final String TAG = "DaddySchemaCreator";
    private final ArrayCompiler arrayCompiler = new ArrayCompiler(this);
    private final ObjectCompiler objectCompiler = new ObjectCompiler(this);
    private final SealedInterfaceCompiler sealedInterfaceCompiler = new SealedInterfaceCompiler(this);
    private final EnumCompiler enumCompiler = new EnumCompiler();
    private final RecordCompiler recordCompiler = new RecordCompiler(this);
    private final Map<TypeContainer<?>, Adapter<?>> adapters;
    public DaddyCompiler(Map<TypeContainer<?>, Function<DaddyCompiler, Adapter<?>>> adapters) {
        this.adapters = adapters.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().apply(this)));
    }

    private final Map<NullableTypeContainer<?>, Schema<?>> compiledSchemas = new ConcurrentHashMap<>();

    /** Non-blocking implementation of {@link ConcurrentHashMap#computeIfAbsent} that cn be called recursively */
    private <T> Schema<T> computeIfAbsentNonBlock(NullableTypeContainer<T> key, Function<NullableTypeContainer<T>, Schema<T>> mappingFunction) {
        var existingValue = compiledSchemas.get(key);
        if (existingValue != null) {
            //noinspection unchecked
            return (Schema<T>) existingValue;
        }
        var newValue = mappingFunction.apply(key);
        var previousValue = compiledSchemas.putIfAbsent(key, newValue);
        //noinspection unchecked
        return previousValue != null ? (Schema<T>) previousValue : newValue;
    }

    public <T> Schema<T> compileToSchema(NullableTypeContainer<T> nullableTypeParam) {
        return computeIfAbsentNonBlock(nullableTypeParam, nullableType -> {
            var type = nullableType.type();
            var nullable = nullableType.nullable();
            Log.d(TAG, "Compiling schema for type: " + type.getTypeName() + ", nullable: " + nullable + " daddy: " + this + " hash code of type: " + type.hashCode());
            if (type.isWildcard() || type.isTypeVariable()) {
                throw new IllegalArgumentException("Cannot compile wildcard or type variable type " + type.getTypeName());
            }
            if (type.isGenericArray()) {
                // Can't define adapters for generic arrays, so do this before adapter check
                return arrayCompiler.compileToSchema(nullableType);
            } else if (!type.isArray()) {
                // Verify that nullableElements is empty for non-array types
                if (nullableType.nullableElements().length != 0) {
                    throw new IllegalArgumentException("Cannot compile non-array type " + type.getTypeName() + " with non-empty nullableElements");
                }
            }
            var rawClass = type.getRawClass();
            for (var entry : adapters.entrySet()) {
                if (satisfiesAdapter(entry.getKey(), type)) {
                    // Adapter strategy
                    //noinspection unchecked
                    return (Schema<T>) ((Adapter<Object>) entry.getValue()).compileToSchema((NullableTypeContainer<Object>) nullableType);
                }
            }
            if (rawClass.isInterface()) {
                if (rawClass.isAnnotation()) {
                    throw new IllegalArgumentException("Cannot compile annotation type " + type.getTypeName());
                }
                if (!rawClass.isSealed()) {
                    throw new IllegalArgumentException("Cannot compile non-sealed interface type " + type.getTypeName() + " without adapter");
                }
                // Sealed interface strategy
                return sealedInterfaceCompiler.compileToSchema(nullableType);
            }
            if (rawClass.isEnum()) {
                // Enum strategy
                return enumCompiler.compileToSchema(nullableType);
            }
            if (rawClass.isRecord()) {
                // Record strategy
                return recordCompiler.compileToSchema(nullableType);
            }
            // normal class
            // arrays have the abstract bit set
            if (Modifier.isAbstract(rawClass.getModifiers()) && !rawClass.isArray()) {
                if (!rawClass.isSealed()) {
                    throw new IllegalArgumentException("Cannot compile non-sealed abstract class type " + type.getTypeName() + " without adapter");
                }
                // Sealed abstract class strategy
                throw new UnsupportedOperationException("Compilation of sealed abstract classes is not yet implemented");
            } else {
                if (rawClass.isSealed()) {
                    // Sealed class strategy
                    throw new UnsupportedOperationException("Compilation of sealed classes is not yet implemented");
                } else {
                    if (type.isVoid()) {
                        if (nullable) {
                            throw new IllegalArgumentException("Cannot compile void type as nullableType as it is already inherently null");
                        }
                        //noinspection unchecked
                        return (Schema<T>) new VoidSchema();
                    } else if (type.isPrimitive()) {
                        if (nullable) {
                            throw new IllegalArgumentException("Cannot compile primitive type " + type.getTypeName() + " as nullableType as it is already inherently non-nullableType");
                        }
                        //noinspection unchecked
                        return (Schema<T>) new PrimitiveSchema<>(type.asPrimitive());
                    } else if (type.isWrapper()) {
                        //noinspection unchecked
                        return (Schema<T>) new WrapperSchema<>(type.asClass(), nullable);
                    } else if (type.isString()) {
                        //noinspection unchecked
                        return (Schema<T>) new StringSchema(nullable);
                    } else if (type.isArray()) {
                        return arrayCompiler.compileToSchema(nullableType);
                    } else if (rawClass == Any.class) {
                        throw new IllegalArgumentException("Cannot compile any type as it is only intended for use in adapters");
                    } else {
                        return objectCompiler.compileToSchema(nullableType);
                    }
                }
            }
        });
    }

    /** Tests if an adapter can serialize a specific type
     * @param adapterMask Which types the adapter can accept
     * @param targetType The type that should be checked
     * @return {@code true} if the adapter can serialize/deserialize an object of the specified type, {@code false} if not
     */
    private static boolean satisfiesAdapter(TypeContainer<?> adapterMask, TypeContainer<?> targetType) {
        return adapterMask.equals(targetType, a -> a.isClass() && a.asClass() == Any.class);
    }
}
