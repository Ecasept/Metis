package dev.ecasept.unitodo.shared.serialization.schemas.array;

import dev.ecasept.unitodo.shared.serialization.GrowableBuffer;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.schemas.Schema;
import dev.ecasept.unitodo.shared.serialization.schemas.SerializationUtils;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.lang.reflect.Array;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/** Includes the instructions for how to (de-)serialize arrays */
public record ArraySchema<T>(TypeContainer<T> type, Schema<?> componentSchema) implements Schema<T> {

    private static final String TAG = "ArraySchema";

    @Override
    public void serialize(T o, GrowableBuffer buf) {
        Object[] arr = (Object[]) o;
        Log.i(TAG, "Serializing array of length: " + arr.length);
        SerializationUtils.serializeLength(arr.length, buf);
        for (Object element : arr) {
            serializeElement(element, buf);
        }
    }

    @SuppressWarnings("unchecked")
    private <E> void serializeElement(Object element, GrowableBuffer buf) {
        ((Schema<E>) componentSchema).serialize((E) element, buf);
    }

    @Override
    public T deserialize(ByteBuffer data) throws SerializationException {
        Class<?> clazz = type.asClass();
        Log.i(TAG, "Deserializing array of type: " + clazz.getName());

        int len;
        try {
            len = SerializationUtils.deserializeLength(data);
            Log.i(TAG, "Deserialized array length: " + len);
        } catch (BufferUnderflowException e) {
            throw new SerializationException("Unexpected end of data while trying to read length for array of class " + clazz.getName(), e);
        }
        if (len < 0) {
            throw new SerializationException("Negative array length " + len + " found while deserializing array of class " + clazz.getName());
        }

        Class<?> componentClass = TypeContainer.of(type.getComponentType()).getRawClass();
        Object[] arr = (Object[]) Array.newInstance(componentClass, len);
        for (int i = 0; i < len; i++) {
            arr[i] = componentSchema.deserialize(data);
        }

        return type.cast(arr);
    }
}