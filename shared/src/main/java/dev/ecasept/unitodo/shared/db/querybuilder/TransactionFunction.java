package dev.ecasept.unitodo.shared.db.querybuilder;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.SQLException;

@FunctionalInterface
public interface TransactionFunction<T> {
    T run() throws SQLException, DatabaseException;
}
