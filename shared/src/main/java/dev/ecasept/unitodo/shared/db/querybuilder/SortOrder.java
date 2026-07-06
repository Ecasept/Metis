package dev.ecasept.unitodo.shared.db.querybuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores a sort order for any SQL clause
 * @param entries A list of columns and their associated desired order
 */
public record SortOrder(List<Entry> entries) {

    /**
     * Stores a single column together with its order
     */
    public sealed interface Entry permits Ascending, Descending {
        /** Returns the associated column */
        String column();
        /** Returns the desired order of the column as a valid SQL representation ("ASC" or "DESC") */
        String order();
    }

    /** Vararg wrapper for constructor */
    public static SortOrder of(Entry... entries) {
        return new SortOrder(List.of(entries));
    }

    /** Creates a new sort order where all the columns provided are sorted in ascending order */
    public static SortOrder ascending(String... columns) {
        return new SortOrder(Stream.of(columns).map(Ascending::new).collect(Collectors.toList()));
    }

    /** Creates a new sort order where all the columns provided are sorted in descending order */
    public static SortOrder descending(String... columns) {
        return new SortOrder(Stream.of(columns).map(Descending::new).collect(Collectors.toList()));
    }

    /** Implements a sort order for a single column in ascending order */
    record Ascending(String column) implements Entry {
        public String order() {
            return "ASC";
        }
    }

    /** Implements a sort order for a single column in descending order */
    record Descending(String column) implements Entry {
        public String order() {
            return "DESC";
        }
    }
}
