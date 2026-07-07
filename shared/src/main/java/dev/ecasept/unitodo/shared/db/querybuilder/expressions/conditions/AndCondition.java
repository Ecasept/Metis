package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.PreparedStatement;

public class AndCondition implements Condition {
    private final Condition c1;
    private final Condition c2;
    public AndCondition(Condition c1, Condition c2) {
        this.c1 = c1;
        this.c2 = c2;
    }
    @Override
    public String asParameterizedSql() {
        return "(" + c1.asParameterizedSql() + " AND " + c2.asParameterizedSql() + ")";
    }
    @Override
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        return c2.fillParameters(statement, c1.fillParameters(statement, i));
    }
}
