package dev.ecasept.unitodo.shared.db.querybuilder.delete;

import dev.ecasept.unitodo.shared.db.DatabaseController;


public class DeleteQueryConfigurator {
    private final DatabaseController controller;

    public DeleteQueryConfigurator(DatabaseController controller) {
        this.controller = controller;
    }

    public DeleteQueryBuilder from(String table) {
        return new DeleteQueryBuilder(controller, table);
    }
}
