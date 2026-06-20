package dev.ecasept.unitodo.shared.db.querybuilder.insert;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class InsertQueryBuilder {
    private final DatabaseController controller;
    private final String table;
    private final List<Object> values;
    private final List<String> columns;
    private ConflictResolver conflictResolver = null;

    public InsertQueryBuilder(DatabaseController controller, String table, List<Object> values, List<String> columns) {
        this.controller = controller;
        this.table = table;
        this.values = values;
        this.columns = columns;
    }

    public ConflictResolverCreator onConflict(String... keys) {
        conflictResolver = new ConflictResolver(List.of(keys));
        return new ConflictResolverCreator(resolverInit -> {
            resolverInit.accept(conflictResolver);
            return this;
        });
    }

    private String asSql() {
        if (values.size() != columns.size()) {
            throw new IllegalArgumentException("Value count must match column count for INSERT INTO");
        }
        var sb = new StringBuilder();
        sb.append("INSERT INTO ").append(BuilderUtils.quoteIdentifier(table));
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Columns or values cannot be empty for INSERT INTO");
        }
        sb.append(" (");
        sb.append(columns.stream().map(BuilderUtils::quoteIdentifier).collect(Collectors.joining(", ")));
        sb.append(")\nVALUES (");
        sb.repeat("?, ", columns.size() - 1).append("?)");
        if (conflictResolver != null) {
            sb.append("\n").append(conflictResolver.asSql());
        }
        return sb.toString();
    }

    public PreparedInsertQuery prepare() throws DatabaseException {
        var statement = controller.prepareStatement(asSql());
        return new PreparedInsertQuery(statement, this::fillParameters);
    }

    private int fillParameters(PreparedStatement statement, int startIndex) throws DatabaseException {
        try {
            int index = startIndex;
            for (Object value : values) {
                BuilderUtils.bindParameter(statement, index++, value);
            }
            return index;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fill parameters for InsertQueryBuilder", e);
        }
    }
}
