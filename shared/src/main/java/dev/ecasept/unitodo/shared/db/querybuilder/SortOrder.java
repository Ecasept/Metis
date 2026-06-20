package dev.ecasept.unitodo.shared.db.querybuilder;

sealed public interface SortOrder permits SortOrder.Ascending, SortOrder.Descending {
    String orderAsSql();
    String[] getColumns();


    record Ascending(String... columns) implements SortOrder {
        public String orderAsSql() {
            return "ASC";
        }
        public String[] getColumns() {
            return columns;
        }
    }
    record Descending(String... columns) implements SortOrder {
        public String orderAsSql() {
            return "DESC";
        }
        public String[] getColumns() {
            return columns;
        }
    }
}
