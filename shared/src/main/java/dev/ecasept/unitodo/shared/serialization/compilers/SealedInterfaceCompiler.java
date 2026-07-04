package dev.ecasept.unitodo.shared.serialization.compilers;

import dev.ecasept.unitodo.shared.serialization.annotations.SerialInstance;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import dev.ecasept.unitodo.shared.serialization.schemas.SealedInterfaceSchema;
import dev.ecasept.unitodo.shared.serialization.types.TypeContainer;

import java.util.HashMap;
import java.util.HashSet;

public class SealedInterfaceCompiler {

    private final DaddyCompiler daddyCompiler;

    public SealedInterfaceCompiler(DaddyCompiler daddyCompiler) {
        this.daddyCompiler = daddyCompiler;
    }

    public <T> SealedInterfaceSchema<T> compileToSchema(NullableTypeContainer<T> nullableType) {
        var type = nullableType.type();
        var clazz = type.asClass();

        if (!clazz.isAnnotationPresent(Serializable.class)) {
            throw new IllegalArgumentException("Tried to (de)serialize non-serializable sealed interface " + clazz.getName());
        }

        HashSet<Integer> seenTags = new HashSet<>();
        HashMap<Class<?>, SealedInterfaceSchema.Implementation<?>> implementationsByClass = new HashMap<>();

        for (var subclass : clazz.getPermittedSubclasses()) {
            checkSingleSealedInterface(subclass, clazz);

            var annotation = getAnnotation(subclass);
            if (seenTags.contains(annotation.tag())) {
                throw new IllegalArgumentException("Found duplicate tag " + annotation.tag() + " in sealed interface " + clazz.getName());
            }
            seenTags.add(annotation.tag());

            var subclassType = TypeContainer.of(subclass);
            var subclassSchema = daddyCompiler.compileToSchema(NullableTypeContainer.of(subclassType, false));

            implementationsByClass.put(subclass, new SealedInterfaceSchema.Implementation<>(annotation.tag(), subclassSchema));
        }

        return new SealedInterfaceSchema<>(implementationsByClass, nullableType.nullable());
    }

    /**
     * Ensures that an implementation only implements a single sealed interface.
     * If serializable implementations could implement multiple sealed interfaces,
     * there would be ambiguity in which sealed interface the {@link SerialInstance} refers to.
     */
    private static void checkSingleSealedInterface(Class<?> subclass, Class<?> expectedSealedInterface) {
        int sealedInterfaceCount = 0;
        for (var i : subclass.getInterfaces()) {
            if (i.isSealed()) {
                sealedInterfaceCount++;
            }
        }
        if (sealedInterfaceCount != 1) {
            throw new IllegalArgumentException(
                    "Permitted subclass " + subclass.getName() + " of sealed interface " + expectedSealedInterface.getName()
                            + " must implement exactly one sealed interface to avoid ambiguity, but implements " + sealedInterfaceCount
            );
        }
    }

    private static SerialInstance getAnnotation(Class<?> subclass) {
        if (!subclass.isAnnotationPresent(SerialInstance.class)) {
            throw new IllegalArgumentException(
                    "Permitted subclass " + subclass.getName() + " is not annotated with @Instance"
            );
        }
        return subclass.getAnnotation(SerialInstance.class);
    }
}