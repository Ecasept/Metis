package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlColumn;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EqualsCondition implements Condition {
    private final SqlExpression exp1;
    private final SqlExpression exp2;
    public EqualsCondition(SqlExpression exp1, SqlExpression exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
    }
    @Override
    public String asParameterizedSql() {
        return "(" + exp1.asParameterizedSql() + " = " + exp2.asParameterizedSql() + ")";
    }
    @Override
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        i = exp1.fillParameters(statement, i);
        i = exp2.fillParameters(statement, i);
        return i;
    }
}
