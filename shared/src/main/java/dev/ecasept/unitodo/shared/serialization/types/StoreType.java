package dev.ecasept.unitodo.shared.serialization.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class StoreType<T> {
    private final Type type;

    public StoreType() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Couldn't store type in StoreType. You need to create an anonymous subclass of StoreType, e.g. `new StoreType<List<String>>() {}`");
        }
        type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
