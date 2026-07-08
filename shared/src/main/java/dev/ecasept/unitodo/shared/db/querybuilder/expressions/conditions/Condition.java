package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.querybuilder.expressions.JavaValueExpression;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlColumn;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.util.Arrays;
import java.util.List;

/** Represents any boolean condition in SQL */
public interface Condition extends SqlExpression {
	/** Combines the current condition with the provided conditions using OR, making it so that one of the provided conditions or this condition must be true */
	default Condition or(Condition... others) {
		if (others.length == 0) {
			throw new IllegalArgumentException("At least one condition is required");
		}
		return Arrays.stream(others).reduce(this, OrCondition::new);
	}
	/** Combines the current condition with the provided conditions using AND, making it so that all the provided conditions and also this condition itself must be true */
	default Condition and(Condition... others) {
		if (others.length == 0) {
			throw new IllegalArgumentException("At least one condition is required");
		}
		return Arrays.stream(others).reduce(this, AndCondition::new);
	}
	/** Requires that, in addition to the current condition, the provided column must contain the provided value */
	default <T> Condition eq(SqlColumn col, T value) { return new AndCondition(this, new EqualsCondition(col, new JavaValueExpression<>(value))); }
	/** Requires that, in addition to the current condition, the provided column must contain a value less than the provided value */
	default <T> Condition lt(SqlColumn col, T value) { return new AndCondition(this, new LessThanCondition(col, new JavaValueExpression<>(value))); }
	/** Requires that, in addition to the current condition, the provided column must contain a value greater than or equal to the provided value */
	default <T> Condition ge(SqlColumn col, T value) { return new AndCondition(this, new GreaterThanOrEqualsCondition(col, new JavaValueExpression<>(value))); }
	/** Requires that, in addition to the current condition, the provided column must contain a value that is equal to one of the provided values */
	default Condition eqAny(SqlColumn col, List<?> values) {
		return new AndCondition(this, values.isEmpty() ? new FalseCondition() : new InCondition<>(col, values));
	}
	/** Requires that, in addition to the current condition, the provided column must contain a value that is equal to one of the provided values */
	default <T> Condition eq(String col, T value) {
		return eq(SqlColumn.of(col), value);
	}
	/** Requires that, in addition to the current condition, the provided column must contain a value less than the provided value */
	default <T> Condition lt(String col, T value) {
		return lt(SqlColumn.of(col), value);
	}
	/** Requires that, in addition to the current condition, the provided column must contain a value greater than or equal to the provided value */
	default <T> Condition ge(String col, T value) {
		return ge(SqlColumn.of(col), value);
	}
	/** Requires that, in addition to the current condition, the provided column must contain a value that is equal to one of the provided values */
	default Condition eqAny(String col, List<?> values) {
		return eqAny(SqlColumn.of(col), values);
	}
}
