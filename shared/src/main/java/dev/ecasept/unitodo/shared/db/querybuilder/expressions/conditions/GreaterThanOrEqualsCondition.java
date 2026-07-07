package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlColumn;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GreaterThanOrEqualsCondition implements Condition {
    private final SqlExpression expr1;
    private final SqlExpression expr2;
    public GreaterThanOrEqualsCondition(SqlExpression expr1, SqlExpression expr2) {
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    @Override
    public String asParameterizedSql() {
        return "(" + expr1.asParameterizedSql() + " >= " + expr2.asParameterizedSql() + ")";
    }
    @Override
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        i = expr1.fillParameters(statement, i);
        i = expr2.fillParameters(statement, i);
        return i;
    }
}

