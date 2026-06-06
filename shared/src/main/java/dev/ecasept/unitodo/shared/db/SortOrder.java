package dev.ecasept.unitodo.shared.db;

public enum SortOrder {
    Ascending ("ASC"),
    Descending ("DESC");

    private final String sql;
    SortOrder(String sql) {
        this.sql = sql;
    }
    public String asSql() {
        return sql;
    }
}
