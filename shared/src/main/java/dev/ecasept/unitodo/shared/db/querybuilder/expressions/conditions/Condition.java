package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.querybuilder.expressions.JavaValueExpression;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlColumn;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.util.Arrays;
import java.util.List;

public interface Condition extends SqlExpression {
	default Condition or(Condition... others) {
		if (others.length == 0) {
			throw new IllegalArgumentException("At least one condition is required");
		}
		return Arrays.stream(others).reduce(this, OrCondition::new);
	}
	default Condition and(Condition... others) {
		if (others.length == 0) {
			throw new IllegalArgumentException("At least one condition is required");
		}
		return Arrays.stream(others).reduce(this, AndCondition::new);
	}
	default <T> Condition eq(SqlColumn col, T value) { return new AndCondition(this, new EqualsCondition(col, new JavaValueExpression<>(value))); }
	default <T> Condition lt(SqlColumn col, T value) { return new AndCondition(this, new LessThanCondition(col, new JavaValueExpression<>(value))); }
	default <T> Condition ge(SqlColumn col, T value) { return new AndCondition(this, new GreaterThanOrEqualsCondition(col, new JavaValueExpression<>(value))); }
	default Condition eqAny(SqlColumn col, List<?> values) {
		return new AndCondition(this, values.isEmpty() ? new FalseCondition() : new InCondition<>(col, values));
	}

	default <T> Condition eq(String col, T value) {
		return eq(SqlColumn.of(col), value);
	}
	default <T> Condition lt(String col, T value) {
		return lt(SqlColumn.of(col), value);
	}
	default <T> Condition ge(String col, T value) {
		return ge(SqlColumn.of(col), value);
	}
	default Condition eqAny(String col, List<?> values) {
		return eqAny(SqlColumn.of(col), values);
	}
}
