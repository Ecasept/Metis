package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.querybuilder.expressions.JavaValueExpression;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlColumn;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.util.Arrays;
import java.util.List;

public final class C {
    public static <T> Condition eq(String col, T value) {
        return eq(SqlColumn.of(col), value);
    }
    public static <T> Condition lt(String col, T value) {
        return lt(SqlColumn.of(col), value);
    }
    public static <T> Condition ge(String col, T value) {
        return ge(SqlColumn.of(col), value);
    }
    public static Condition eqAny(String col, List<?> values) {
        return eqAny(SqlColumn.of(col), values);
    }
    public static Condition contains(String col, String pattern) {
        return contains(SqlColumn.of(col), pattern);
    }

    public static Condition eq(SqlColumn col, SqlExpression expr) { return new EqualsCondition(col, expr); }
    public static <T> Condition eq(SqlColumn col, T value) { return new EqualsCondition(col, new JavaValueExpression<>(value)); }
    public static Condition lt(SqlColumn col, SqlExpression expr) { return new LessThanCondition(col, expr); }
    public static <T> Condition lt(SqlColumn col, T value) { return new LessThanCondition(col, new JavaValueExpression<>(value)); }
    public static Condition ge(SqlColumn col, SqlExpression expr) { return new GreaterThanOrEqualsCondition(col, expr); }
    public static <T> Condition ge(SqlColumn col, T value) { return new GreaterThanOrEqualsCondition(col, new JavaValueExpression<>(value)); }
    public static Condition eqAny(SqlColumn col, List<?> values) {
        return values.isEmpty() ? new FalseCondition() : new InCondition<>(col, values);
    }
    public static Condition contains(SqlColumn col, String pattern) { return new LikeCondition(col, "%" + pattern + "%"); }
    public static Condition not(Condition c) { return new NotCondition(c); }
    public static Condition and(Condition... conditions) {
        if (conditions.length == 0) {
            throw new IllegalArgumentException("At least one condition is required");
        }
        return Arrays.stream(conditions).reduce(Condition::and).orElseThrow();
    }
    public static Condition or(Condition... conditions) {
        if (conditions.length == 0) {
            throw new IllegalArgumentException("At least one condition is required");
        }
        return Arrays.stream(conditions).reduce(Condition::or).orElseThrow();
    }
}
