package dev.ecasept.unitodo.shared.db.querybuilder;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.delete.DeleteQueryConfigurator;
import dev.ecasept.unitodo.shared.db.querybuilder.insert.InsertQueryConfigurator;
import dev.ecasept.unitodo.shared.db.querybuilder.select.SelectQueryConfigurator;
import dev.ecasept.unitodo.shared.utils.ThrowingBiSupplier;

import java.sql.SQLException;

/** Base class of the query builder, providing the entry point for building all queries */
public class QueryBuilder {
    private final DatabaseController controller;

    /** Creates a query builder that controls the database through the given controller. */
    public QueryBuilder(DatabaseController controller) {
        this.controller = controller;
    }

    /** Returns a builder for a select query.
     * @param columns The columns to select. If no columns are specified, all columns will be selected.
     */
    public SelectQueryConfigurator select(String... columns) {
        return new SelectQueryConfigurator(controller, java.util.List.of(columns));
    }

    /** Returns a builder for an insert query. */
    public InsertQueryConfigurator insert() {
        return new InsertQueryConfigurator(controller);
    }

    /** Returns a builder for a delete query. */
    public DeleteQueryConfigurator delete() {
        return new DeleteQueryConfigurator(controller);
    }

    /**
     * Executes a transaction with the given function.
     * A transaction is executed in one go, and if any exception occurs, any changes made during the transaction are rolled back.
     * If an exception occurs during the rollback, the database might be left in an inconsistent state.
     * @param fn The function executing the database operations that should be executed in a transaction.
     * @return The result of the function.
     * @param <T> The return type of the function.
     * @throws DatabaseException If something goes wrong during the transaction or rollback, or if the transaction function throws a DatabaseException.
     * @throws SQLException If the transaction function throws an SQLException.
     */
    public <T> T transaction(ThrowingBiSupplier<T, DatabaseException, SQLException> fn) throws DatabaseException, SQLException {
        controller.setAutoCommit(false);

        Throwable rootException = null;
        try {
            var res = fn.get();
            controller.commitTransaction();
            return res;
        } catch (Throwable e) {
            rootException = e;
            try {
                controller.rollbackTransaction();
            } catch (Exception rollbackException) {
                rootException.addSuppressed(rollbackException);
            }
            throw e;
        } finally {
            try {
                controller.setAutoCommit(true);
            } catch (Exception autoCommitException) {
                if (rootException != null) {
                    rootException.addSuppressed(autoCommitException);
                } else {
                    throw autoCommitException;
                }
            }
        }
    }
}