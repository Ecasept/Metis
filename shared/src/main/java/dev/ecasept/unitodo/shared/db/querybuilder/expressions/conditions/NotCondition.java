package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.PreparedStatement;

public class NotCondition implements Condition {
    private final Condition c;
    public NotCondition(Condition c) {
        this.c = c;
    }
    public String asParameterizedSql() {
        return "( NOT " + c.asParameterizedSql() + " )";
    }
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        return c.fillParameters(statement, i);
    }
}
