package dev.ecasept.unitodo.shared.db.querybuilder.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.PreparedStatement;

public interface Condition {
    String asParameterizedSql();
    int fillParameters(PreparedStatement statement, int i) throws DatabaseException;
}
