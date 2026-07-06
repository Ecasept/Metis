package dev.ecasept.unitodo.shared.serialization.compilers;

import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.schemas.array.ArraySchema;
import dev.ecasept.unitodo.shared.serialization.schemas.array.PrimitiveArraySchema;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

public class ArrayCompiler {
    private final DaddyCompiler daddyCompiler;
    public ArrayCompiler(DaddyCompiler daddyCompiler) {
        this.daddyCompiler = daddyCompiler;
    }

    <T> Schema<T> compileToSchema(NullableTypeContainer<T> nullableType) {
        var type = nullableType.type();
        var nullableElements = nullableType.nullableElements();
        var arrDim = nullableType.arrDim();
        var componentType = TypeContainer.of(type.getComponentType());
        if (!componentType.isArray()) {
            if (arrDim + 1 < nullableElements.length) {
                throw new IllegalArgumentException("Too many dimensions in nullableElements for array type " + type.getTypeName() + ". Expected at most " + (arrDim + 1) + " but got " + nullableElements.length);
            }
        }

        var elementNullable = arrDim < nullableElements.length && nullableElements[arrDim];

        if (componentType.isPrimitive()) {
            // fast path for primitive arrays
            return new PrimitiveArraySchema<>(type);
        }

        var componentSchema = daddyCompiler.compileToSchema(new NullableTypeContainer<>(componentType, elementNullable, nullableElements, arrDim + 1));
        return new ArraySchema<>(type, componentSchema);
    }
}
