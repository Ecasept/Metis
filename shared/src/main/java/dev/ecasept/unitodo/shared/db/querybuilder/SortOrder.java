package dev.ecasept.unitodo.shared.db.querybuilder;

sealed public interface SortOrder permits SortOrder.Ascending, SortOrder.Descending {
    String orderAsSql();
    String column();


    record Ascending(String column) implements SortOrder {
        public String orderAsSql() {
            return "ASC";
        }
    }
    record Descending(String column) implements SortOrder {
        public String orderAsSql() {
            return "DESC";
        }
    }
}
