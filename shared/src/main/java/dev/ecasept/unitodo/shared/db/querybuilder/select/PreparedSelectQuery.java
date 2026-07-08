package dev.ecasept.unitodo.shared.db.querybuilder.select;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.FillParameters;
import dev.ecasept.unitodo.shared.utils.ThrowingFunction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

public class PreparedSelectQuery implements AutoCloseable {
    private final PreparedStatement statement;
    private final FillParameters fn;
    public PreparedSelectQuery(PreparedStatement statement, FillParameters fn) {
        this.statement = statement;
        this.fn = fn;
    }

    public ResultSet execute() throws DatabaseException, SQLException {
        fn.fillParameters(statement, 1);
        return statement.executeQuery();
    }
    public <T> Optional<T> executeSingle(ThrowingFunction<ResultSet, T, SQLException> converter) throws SQLException, DatabaseException {
        var rs = execute();
        if (!rs.next()) {
            return Optional.empty();
        }
        return Optional.of(converter.apply(rs));
    }
    public <T> ArrayList<T> executeMulti(ThrowingFunction<ResultSet, T, SQLException> converter) throws SQLException, DatabaseException {
        var rs = execute();
        var items = new ArrayList<T>();
        while (rs.next()) {
            items.add(converter.apply(rs));
        }
        return items;
    }
    @Override
    public void close() throws SQLException {
        statement.close();
    }
}
