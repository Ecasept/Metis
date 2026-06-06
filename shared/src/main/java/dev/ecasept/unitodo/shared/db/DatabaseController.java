package dev.ecasept.unitodo.shared.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseController {
    private final Connection connection;
    private final SqlRunner sqlRunner;
    public DatabaseController(String path) throws DatabaseException {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to connect to database at path: " + path, e);
        }
        this.sqlRunner = new SqlRunner(connection);
        sqlRunner.runSql("schema.sql");
    }

    public PreparedStatement prepareStatement(String sql) throws DatabaseException {
        try {
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to prepare SQL statement: " + sql, e);
        }
    }

    public void close() throws DatabaseException {
        try {
            connection.close();
        } catch (Exception e) {
            throw new DatabaseException("Failed to close database connection", e);
        }
    }
}
