package dev.ecasept.unitodo.shared.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to mark an instance of an enum or a permitted instance of a sealed interface for serialization. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE_USE})
public @interface SerialInstance {
    /** The unique identifier for this instance in the serialized data. */
    int tag();
}
