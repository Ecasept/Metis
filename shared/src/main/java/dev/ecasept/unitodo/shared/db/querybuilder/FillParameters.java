package dev.ecasept.unitodo.shared.db.querybuilder;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.PreparedStatement;

@FunctionalInterface
public interface FillParameters {
        int fillParameters(PreparedStatement statement, int i) throws DatabaseException;
}
