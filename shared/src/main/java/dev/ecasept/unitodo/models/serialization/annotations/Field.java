package dev.ecasept.unitodo.models.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to mark a field for serialization. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Field {
    /** The unique identifier for the field in the serialized data. */
    int tag();
    /** Whether the field is optional (default: false).
     * If true, the field may be omitted from the serialized data
     * and will be set to null or default value when deserialized if not present. */
    boolean optional() default false;
    /** Whether the field can be null (default: false).
     * If true, the field can be set to null during serialization
     * and can be deserialized as null if the serialized data indicates a null value. */
    boolean nullable() default false;
    /**
     * Whether nullable elements of an array can be null.
     */
    boolean[] nullableElements() default {};
}
