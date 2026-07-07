package dev.ecasept.unitodo.shared.db.querybuilder;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.delete.DeleteQueryConfigurator;
import dev.ecasept.unitodo.shared.db.querybuilder.insert.InsertQueryConfigurator;
import dev.ecasept.unitodo.shared.db.querybuilder.select.SelectQueryConfigurator;
import dev.ecasept.unitodo.shared.utils.ThrowingSupplier2;

import java.sql.SQLException;

public class QueryBuilder {
    private final DatabaseController controller;

    public QueryBuilder(DatabaseController controller) {
        this.controller = controller;
    }

    public SelectQueryConfigurator select(String... columns) {
        return new SelectQueryConfigurator(controller, java.util.List.of(columns));
    }

    public InsertQueryConfigurator insert() {
        return new InsertQueryConfigurator(controller);
    }

    public DeleteQueryConfigurator delete() {
        return new DeleteQueryConfigurator(controller);
    }

    public <T> T transaction(ThrowingSupplier2<T, DatabaseException, SQLException> fn) throws DatabaseException, SQLException {
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