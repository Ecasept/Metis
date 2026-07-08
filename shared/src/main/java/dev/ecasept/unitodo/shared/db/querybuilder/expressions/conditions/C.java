package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.querybuilder.expressions.JavaValueExpression;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlColumn;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for building SQL conditions
 */
public final class C {
    /** Returns a new condition that requries that the provided column is equal to the provided value */
    public static <T> Condition eq(String col, T value) {
        return eq(SqlColumn.of(col), value);
    }
    /** Returns a new condition that requires that the provided column is less than the provided value */
    public static <T> Condition lt(String col, T value) {
        return lt(SqlColumn.of(col), value);
    }
    /** Returns a new condition that requires that the provided column is greater than or equal to the provided value */
    public static <T> Condition ge(String col, T value) {
        return ge(SqlColumn.of(col), value);
    }
    /** Returns a new condition that requires that the provided column is equal to any of the provided values */
    public static Condition eqAny(String col, List<?> values) {
        return eqAny(SqlColumn.of(col), values);
    }
    /** Returns a new condition that requires that the provided column contains the provided string */
    public static Condition contains(String col, String pattern) {
        return contains(SqlColumn.of(col), pattern);
    }

    /** Returns a new condition that requires that the provided column is equal to the provided expression */
    public static Condition eq(SqlColumn col, SqlExpression expr) { return new EqualsCondition(col, expr); }
    /** Returns a new condition that requires that the provided column is equal to the provided value */
    public static <T> Condition eq(SqlColumn col, T value) { return new EqualsCondition(col, new JavaValueExpression<>(value)); }
    /** Returns a new condition that requires that the provided column is less than the provided expression */
    public static Condition lt(SqlColumn col, SqlExpression expr) { return new LessThanCondition(col, expr); }
    /** Returns a new condition that requires that the provided column is less than the provided value */
    public static <T> Condition lt(SqlColumn col, T value) { return new LessThanCondition(col, new JavaValueExpression<>(value)); }
    /** Returns a new condition that requires that the provided column is greater than or equal to the provided expression */
    public static Condition ge(SqlColumn col, SqlExpression expr) { return new GreaterThanOrEqualsCondition(col, expr); }
    /** Returns a new condition that requires that the provided column is greater than or equal to the provided value */
    public static <T> Condition ge(SqlColumn col, T value) { return new GreaterThanOrEqualsCondition(col, new JavaValueExpression<>(value)); }
    /** Returns a new condition that requires that the provided column is equal to any of the provided values */
    public static Condition eqAny(SqlColumn col, List<?> values) {
        return values.isEmpty() ? new FalseCondition() : new InCondition<>(col, values);
    }
    /** Returns a new condition that requires that the provided column contains the provided string */
    public static Condition contains(SqlColumn col, String pattern) { return new LikeCondition(col, "%" + pattern + "%"); }
    /** Returns a new condition that requires the provided condition to be false */
    public static Condition not(Condition c) { return new NotCondition(c); }
    /** Returns a new condition that requires all the provided conditions to be true */
    public static Condition and(Condition... conditions) {
        if (conditions.length == 0) {
            throw new IllegalArgumentException("At least one condition is required");
        }
        return Arrays.stream(conditions).reduce(Condition::and).orElseThrow();
    }
    /** Returns a new condition that requires at least one of the provided conditions to be true */
    public static Condition or(Condition... conditions) {
        if (conditions.length == 0) {
            throw new IllegalArgumentException("At least one condition is required");
        }
        return Arrays.stream(conditions).reduce(Condition::or).orElseThrow();
    }
}
