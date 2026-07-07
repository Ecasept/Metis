package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class InCondition<T> implements Condition {
    private final SqlExpression expr;
    private final List<T> options;

    public InCondition(SqlExpression expr, List<T> options) {
        this.expr = expr;
        this.options = options;
    }

    @Override
    public String asParameterizedSql() {
        return "(" + expr.asParameterizedSql() + " IN (" + "?, ".repeat(options.size() - 1) + "?))";
    }

    @Override
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        try {
            i = expr.fillParameters(statement, i);
            for (var option : options) {
                BuilderUtils.bindParameter(statement, i++, option);
            }
            return i;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fill parameters for InCondition", e);
        }
    }
}
