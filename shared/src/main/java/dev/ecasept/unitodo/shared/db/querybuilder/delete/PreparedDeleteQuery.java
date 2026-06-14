package dev.ecasept.unitodo.shared.db.querybuilder.delete;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.FillParameters;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedDeleteQuery implements AutoCloseable {
    private final PreparedStatement statement;
    private final FillParameters fn;
    public PreparedDeleteQuery(PreparedStatement statement, FillParameters fn) {
        this.statement = statement;
        this.fn = fn;
    }

    public int execute() throws DatabaseException, SQLException {
        fn.fillParameters(statement, 1);
        return statement.executeUpdate();
    }
    @Override
    public void close() throws SQLException {
        statement.close();
    }
}
