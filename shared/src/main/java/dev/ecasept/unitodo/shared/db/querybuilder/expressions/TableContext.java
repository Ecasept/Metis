package dev.ecasept.unitodo.shared.db.querybuilder.expressions;

/** Context on the current table, including utils */
public class TableContext {
    private final String table;

    public TableContext(String table) {
        this.table = table;
    }

    /** Returns an expression representing a column in the current table. */
    public SqlColumn col(String name) {
        return SqlColumn.of(table, name);
    }

    /** Returns an expression representing a column in the excluded table (used in upsert statements). */
    public SqlColumn excluded(String name) {
        return SqlColumn.of("excluded", name);
    }
}

