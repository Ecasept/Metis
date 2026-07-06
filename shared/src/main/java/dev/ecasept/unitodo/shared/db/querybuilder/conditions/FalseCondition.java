package dev.ecasept.unitodo.shared.db.querybuilder.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.PreparedStatement;

public class FalseCondition implements Condition {
    public FalseCondition() {
    }
    public String asParameterizedSql() {
        return "(1=0)";
    }
    public int fillParameters(PreparedStatement statement, int i) {
        return i;
    }
}
