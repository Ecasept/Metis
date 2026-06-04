package dev.ecasept.unitodo.shared.serialization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to mark a field for serialization. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface Field {
    /** The unique identifier for the field in the serialized data. */
    int tag();
    /** Whether the field is optional (default: {@code false}).
     * If true, the field may be omitted from the serialized data
     * and will be set to null or default value when deserialized if not present. */
    boolean optional() default false;
    /** Whether the field can be null (default: {@code false}).
     * If true, the field can be set to null during serialization
     * and can be deserialized as null if the serialized data indicates a null value. */
    boolean nullable() default false;
    /**
     * Specifies whether the elements at each array dimension may be {@code null}.
     *
     * <p>For example, consider the field:
     *
     * <pre>{@code
     * @Field(tag = 1, nullableElements = {true, false, true, false})
     * int[][][][] x;
     * }</pre>
     *
     * <p>This indicates that:
     *
     * <ul>
     *   <li>The first dimension may contain {@code null} arrays.</li>
     *   <li>The second dimension may not contain {@code null} arrays.</li>
     *   <li>The third dimension may contain {@code null} arrays.</li>
     *   <li>The fourth dimension may not contain {@code null} values.</li>
     * </ul>
     *
     * <p>Consequently:
     *
     * <ul>
     *   <li>{@code x} itself may be {@code null}, depending on the field's
     *       {@code nullable} property.</li>
     *   <li>{@code x[i]} may be {@code null}.</li>
     *   <li>{@code x[i][j]} may not be {@code null}.</li>
     *   <li>{@code x[i][j][k]} may be {@code null}.</li>
     *   <li>{@code x[i][j][k][l]} may not be {@code null}.</li>
     * </ul>
     *
     * <p>Note that for primitive element types (such as {@code int} in the
     * example above), the final dimension cannot actually contain
     * {@code null} values. This constraint is enforced by the serializer
     * at runtime. Setting a {@code true} value for a dimension that contains primitive elements will result in a runtime error during serialization or deserialization.
     */
    boolean[] nullableElements() default {};
}
