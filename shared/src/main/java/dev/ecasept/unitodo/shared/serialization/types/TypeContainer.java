package dev.ecasept.unitodo.shared.serialization.types;


import java.lang.reflect.*;

public class TypeContainer<T> {
    private final Type type;
    public TypeContainer(Type type) {
        this.type = type;
    }
    public <R extends StoreType<T>> TypeContainer(R storeType) {
        this.type = storeType.getType();
    }

    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        return (T) obj;
    }
    
    public Field[] getFields() {
        if (type instanceof Class<?> clazz) {
            return clazz.getDeclaredFields();
        } else if (type instanceof ParameterizedType paramType) {
            Type rawType = paramType.getRawType();
            if (rawType instanceof Class<?> clazz) {
                return clazz.getDeclaredFields();
            } else {
                throw new IllegalArgumentException("Unsupported raw type: " + rawType);
            }
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    public boolean isVoid() {
        if (type instanceof Class<?> clazz) {
            return clazz == Void.class || clazz == void.class;
        }
        return false;
    }
    public boolean isPrimitive() {
        if (type instanceof Class<?> clazz) {
            return clazz.isPrimitive();
        }
        return false;
    }
    public Class<?> asPrimitive() {
        if (type instanceof Class<?> clazz) {
            if (clazz.isPrimitive()) {
                return clazz;
            }
        }
        throw new IllegalArgumentException("Type is not a primitive: " + type);
    }
    public boolean isArray() {
        if (type instanceof Class<?> clazz) {
            return clazz.isArray();
        } else return type instanceof GenericArrayType arrayType;
    }
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
    public boolean isClass() {
        return type instanceof Class<?>;
    }
    public Class<?> asClass() {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        throw new IllegalArgumentException("Type is not a class: " + type);
    }
    public Class<?> getRawClass() {
        if (type instanceof Class<?> clazz) {
            return clazz;
        } else if (type instanceof ParameterizedType paramType) {
            return (Class<?>) paramType.getRawType(); // This cast is safe because the raw type of a parameterized type is always a class
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
    public boolean isParameterized() {
        return type instanceof ParameterizedType;
    }
    public ParameterizedType asParameterized() {
        if (type instanceof ParameterizedType paramType) {
            return paramType;
        }
        throw new IllegalArgumentException("Type is not a parameterized type: " + type);
    }
    public boolean isWildcard() {
        return type instanceof WildcardType;
    }
    public boolean isTypeVariable() {
        return type instanceof TypeVariable;
    }
    public boolean isString() {
        if (type instanceof Class<?> clazz) {
            return clazz == String.class;
        }
        return false;
    }
    public boolean isWrapper() {
        if (type instanceof Class<?> clazz) {
            return clazz == Boolean.class || clazz == Byte.class || clazz == Character.class ||
                   clazz == Short.class || clazz == Integer.class || clazz == Long.class ||
                   clazz == Float.class || clazz == Double.class;
        }
        return false;
    }
    public String getTypeName() {
        return type.getTypeName();
    }

    public TypeContainer<?> getFieldType(Field field) {
        Type fieldType = field.getGenericType();
        switch (fieldType) {
            case Class<?> clazz -> {
                return new TypeContainer<>(clazz);
            }
            case ParameterizedType paramType -> {
                return new TypeContainer<>(paramType);
            }
            case GenericArrayType arrayType -> {
                return new TypeContainer<>(arrayType);
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
                            return new TypeContainer<>(typeArgs[i]);
                        }
                    }
                }
                throw new IllegalArgumentException("Cannot resolve type variable: " + typeVar);
            }
            default -> throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }
}
