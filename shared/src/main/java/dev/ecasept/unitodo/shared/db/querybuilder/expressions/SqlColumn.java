package dev.ecasept.unitodo.shared.db.querybuilder.expressions;

import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;

import java.sql.PreparedStatement;
import java.util.Optional;


/** Represents an SQL column, optionally qualified with a table name. */
public record SqlColumn(String columnName, Optional<String> tableName) implements SqlExpression {
    /** Returns the full qualified name of the column, including the table name if present. */
    public String getQualifiedName() {
        if (tableName.isEmpty() || tableName.get().isBlank()) {
            return BuilderUtils.quoteIdentifier(columnName);
        }
        return BuilderUtils.quoteIdentifier(tableName.get()) + "." + BuilderUtils.quoteIdentifier(columnName);
    }

    /** Creates a new unqualified SqlColumn with the given column name. */
    public static SqlColumn of(String columnName) {
        return new SqlColumn(columnName, Optional.empty());
    }

    /** Creates a new qualified SqlColumn with the given table and column names. */
    public static SqlColumn of(String tableName, String columnName) {
        return new SqlColumn(columnName, Optional.of(tableName));
    }


    public String asParameterizedSql() {
        return getQualifiedName();
    }

    public int fillParameters(PreparedStatement statement, int index) {
        return index;
    }
}
