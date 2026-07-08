package dev.ecasept.unitodo.shared.db.querybuilder.select;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import java.util.List;

/** Intermediate state of the select query builder that allows specifying the table to select from. */
public class SelectQueryConfigurator {
    private final DatabaseController controller;
    private final List<String> columns;

    public SelectQueryConfigurator(DatabaseController controller, List<String> columns) {
        this.controller = controller;
        this.columns = columns;
    }

    /** Specifies from which table to select the columns. */
    public SelectQueryBuilder from(String table) {
        return new SelectQueryBuilder(controller, table, columns);
    }
}
