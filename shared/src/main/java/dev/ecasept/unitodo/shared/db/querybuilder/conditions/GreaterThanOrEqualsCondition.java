package dev.ecasept.unitodo.shared.db.querybuilder.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GreaterThanOrEqualsCondition<T> implements Condition {
    private final String column;
    private final T operand;
    public GreaterThanOrEqualsCondition(String column, T operand) {
        this.column = column;
        this.operand = operand;
    }
    public String asParameterizedSql() {
        return "(" + BuilderUtils.quoteIdentifier(column) + " >= ?)";
    }
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        try {
            statement.setObject(i, operand);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fill parameters for GreaterOrEqualsCondition", e);
        }
        return i + 1;
    }
}

