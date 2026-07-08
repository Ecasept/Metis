package dev.ecasept.unitodo.shared.db.querybuilder.expressions;


import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Represents a Java value as an SQl expression */
public class JavaValueExpression<T> implements SqlExpression {
    private final T value;
    public JavaValueExpression(T value) {
        this.value = value;
    }
    @Override
    public String asParameterizedSql() {
        return "?";
    }
    @Override
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        try {
            BuilderUtils.bindParameter(statement, i, value);
            return i + 1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fill parameters for JavaValueExpression", e);
        }
    }
}
