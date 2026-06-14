package dev.ecasept.unitodo.shared.db.querybuilder.insert;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.FillParameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedInsertQuery implements AutoCloseable {
    private final PreparedStatement statement;
    private final FillParameters fn;
    public PreparedInsertQuery(PreparedStatement statement, FillParameters fn) {
        this.statement = statement;
        this.fn = fn;
    }

    public int execute() throws SQLException, DatabaseException {
        fn.fillParameters(statement, 1);
        return statement.executeUpdate();
    }

    @Override
    public void close() throws SQLException {
        statement.close();
    }
}
