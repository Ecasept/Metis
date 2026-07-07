package dev.ecasept.unitodo.shared.db;

import dev.ecasept.unitodo.shared.utils.Log;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/** Controller for the database that manages the connection and provides methods for executing SQL statements. */
public class DatabaseController implements AutoCloseable {
    private final Connection connection;
    private static final String TAG = "DatabaseController";

    public DatabaseController(ClassLoader classLoader, String url) throws DatabaseException {
        try {
            var config = new SQLiteConfig();
            config.setJournalMode(SQLiteConfig.JournalMode.WAL);
            config.setBusyTimeout(5000);
            config.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
            this.connection = DriverManager.getConnection(url, config.toProperties());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to connect to database at url: " + url, e);
        }
        var sqlRunner = new SqlRunner(connection);
        try {
            sqlRunner.runSql(classLoader, "schema.sql");
        } catch (DatabaseException e) {
            throw new DatabaseException("Failed to initialize database schema", e);
        }
    }

    /** Prepares a given SQL statement */
    public PreparedStatement prepareStatement(String sql) throws DatabaseException {
        try {
            Log.i(TAG, "Preparing SQL statement: " + sql);
            return connection.prepareStatement(sql);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to prepare SQL statement: " + sql, e);
        }
    }

    /** Changes the auto commit configuration of the database connection.
     * See {@link Connection#setAutoCommit(boolean)} for more information.
     */
    public void setAutoCommit(boolean autoCommit) throws DatabaseException {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to set auto-commit to " + autoCommit, e);
        }
    }

    /** Commits the current transaction */
    public void commitTransaction() throws DatabaseException {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to commit transaction", e);
        }
    }

    /** Rolls back the current transaction */
    public void rollbackTransaction() throws DatabaseException {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to rollback transaction", e);
        }
    }

    /** Closes the database connection */
    @Override
    public void close() throws DatabaseException {
        try {
            connection.close();
        } catch (Exception e) {
            throw new DatabaseException("Failed to close database connection", e);
        }
    }
}
