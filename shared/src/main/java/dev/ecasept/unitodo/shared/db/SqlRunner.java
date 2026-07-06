package dev.ecasept.unitodo.shared.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlRunner {
    private final Connection connection;
    public SqlRunner(Connection connection) {
        this.connection = connection;
    }

    /** Runs an SQL file on the database
     *
     * @param classLoader The classloader with access to the SQL file as a resource
     * @param path The path to the SQL file as a resource
     * @throws DatabaseException If an error occurs
     */
    public void runSql(ClassLoader classLoader, String path) throws DatabaseException {
        try (InputStream is = classLoader.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("SQL file not found at path: " + path);
            }
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            String[] statements = sql.split(";");
            for (String stmt : statements) {
                stmt = stmt.trim();
                if (!stmt.isEmpty()) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(stmt);
                    }
                }
            }
        } catch (IOException e) {
            throw new DatabaseException("Failed to read SQL file: " + path, e);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to execute SQL from file: " + path, e);
        }
    }
}
