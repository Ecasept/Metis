package dev.ecasept.unitodo.shared.db.querybuilder.expressions;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.PreparedStatement;

public interface SqlExpression {
    String asParameterizedSql();
    int fillParameters(PreparedStatement statement, int i) throws DatabaseException;
}
