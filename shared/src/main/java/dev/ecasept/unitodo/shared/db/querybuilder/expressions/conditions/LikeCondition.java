package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlColumn;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LikeCondition implements Condition {
    private final SqlExpression expr;
    private final String pattern;
    public LikeCondition(SqlExpression expr, String pattern) {
        this.expr = expr;
        this.pattern = pattern;
    }
    public String asParameterizedSql() {
        return "(" + expr.asParameterizedSql() + " LIKE ?)";
    }
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
         try {
             i = expr.fillParameters(statement, i);
             statement.setString(i, pattern);
             return i + 1;
         } catch (SQLException e) {
             throw new DatabaseException("Failed to fill parameters for LikeCondition", e);
         }
    }
}
