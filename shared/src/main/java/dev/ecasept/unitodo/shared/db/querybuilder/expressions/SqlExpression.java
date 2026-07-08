package dev.ecasept.unitodo.shared.db.querybuilder.expressions;

import dev.ecasept.unitodo.shared.db.DatabaseException;

import java.sql.PreparedStatement;

/** Represents any valid SQL expression */
public interface SqlExpression {
    /** Returns the SQL representation of this expression with user provided values replaced with placeholders*/
    String asParameterizedSql();

    /** Fills in the user-provided values.
     *
     * @param statement The prepared statement containig the placeholders
     * @param i The index of the first placeholder to fill in
     * @return The index of the next placeholder to fill in
     * @throws DatabaseException If an error occurs while filling in the parameters
     */
    int fillParameters(PreparedStatement statement, int i) throws DatabaseException;
}
