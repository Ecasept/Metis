package dev.ecasept.unitodo.shared.db.querybuilder.insert;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import java.util.ArrayList;
import java.util.List;

public class InsertQueryConfigurator {
    private final DatabaseController controller;
    private final List<String> columns = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    public InsertQueryConfigurator(DatabaseController controller) {
        this.controller = controller;
    }

    public InsertQueryConfigurator v(String column, Object value) {
        this.columns.add(column);
        this.values.add(value);
        return this;
    }

    public InsertQueryBuilder into(String table) {
        return new InsertQueryBuilder(controller, table, values, columns);
    }
}
