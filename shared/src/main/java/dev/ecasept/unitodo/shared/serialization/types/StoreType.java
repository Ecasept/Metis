package dev.ecasept.unitodo.shared.serialization.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Implementation of the super type token pattern.
 * This allows us to store types including the generic type parameters such that they won't be erased at runtime.
 * <h3>Usage</h3>
 * <pre>{@code
 * // Create an anonymous subclass of this class to store the type:
 * StoreType<?> storedType = new StoreType<HashMap<Integer, List<String>>>() {};
 * // Extract the type and generic args:
 * Type type = storedType.getType();
 * System.out.println("Stored the type " + type); // prints "java.util.HashMap<java.lang.Integer, java.util.List<java.lang.String>>"
 * System.out.println("First generic arg " + ((ParameterizedType) type).getActualTypeArguments()[0]); // prints "class java.lang.Integer"
 * System.out.println("Second generic arg " + ((ParameterizedType) type).getActualTypeArguments()[1]); // prints "java.util.List<java.lang.String>"
 * }</pre>
 * <h3>Explanation</h3>
 * The expression {@code List<String>.class} is syntactically invalid in java, as the generic type parameter would be erased at runtime and therefore cannot be stored.
 * <br>
 * A {@code Class<?>} object can only store a single class, but not the generic type parameters.
 * <br>
 * Therefore, we need a {@link java.lang.reflect.Type} object to store the type, and a way to capture the generic types into a {@link Type} object.
 * <p>
 * Now while we could just create our own {@link Type} objects, that would be very cumbersome to do.
 * <br>
 * We would ideally want to use Java's generic syntax (the one with the angle brackets), but Java erases all of that information at runtime.
 * <br>
 * However, there is a loophole: While Java erases types on every instance
 * (collapsing an instance where {@code ArrayList<T>} was set to {@code ArrayList<String>} down to {@code ArrayList}),
 * it actually preserves information written in a class definition.
 * <pre>{@code
 *     class MyClass extends SuperClass<List<String>> {}
 *     // MyClass.class.getGenericSuperclass() will return a `ParameterizedType` representing `SuperClass<List<String>>`;
 * }</pre>
 * <p>
 * Our class {@code MyClass} now effectively stores the type {@code List<String>} in its superclass definition for further usage during runtime,
 * all using a nice angle bracket syntax.
 * This means that we found a way to get a {@link Type} object (that can represent generic types) while using a nice and convenient built-in syntax.
 * By creating a new subclass for every type we want to store, we can now save generic types until runtime.
 * Using a bit of syntactic sugar (namely anonymous subclasses), we can reduce a good chunk of the subclass definition and make storing types a lot more convenient.
 * <p>
 * This class is an implementation of the {@code SuperClass} in the previous example that provides some boilerplate code to extract the preserved generic type parameters.
 *
 * @param <T> The type to store, including generic type parameters, e.g. {@code List<String>}.
 */
public abstract class StoreType<T> {
    private final Type type;

    /**
     * Instantiates a new {@link StoreType} and captures the generic type parameter {@link T} into a {@link Type} object.
     * This should never be called directly, but instead an anonymous subclass should be created, e.g. {@code new StoreType<List<String>> () {}}
     */
    protected StoreType() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new RuntimeException("Missing generic type parameter. You must create an anonymous subclass of StoreType, e.g., `new StoreType<List<String>>() {}`.");
        }
        type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * Returns the stored type, including generic type parameters, e.g. {@code List<String>}.
     * @return The stored type.
     */
    public Type getType() {
        return type;
    }
}
