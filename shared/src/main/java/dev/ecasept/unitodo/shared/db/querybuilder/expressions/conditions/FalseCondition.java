package dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions;

import java.sql.PreparedStatement;

public class FalseCondition implements Condition {
    @Override
    public String asParameterizedSql() {
        return "(1=0)";
    }
    @Override
    public int fillParameters(PreparedStatement statement, int i) {
        return i;
    }
}
