package dev.ecasept.unitodo.shared.db.querybuilder.select;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import dev.ecasept.unitodo.shared.db.querybuilder.SortOrder;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.TableContext;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions.Condition;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/** A builder that allows you to build a SELECT query with various options */
public class SelectQueryBuilder {
    private final DatabaseController controller;
    private final String table;
    private final List<String> columns;
    private SortOrder sortOrder = null;
    public SelectQueryBuilder(DatabaseController controller, String table, List<String> columns) {
        this.controller = controller;
        this.table = table;
        this.columns = columns;
    }
    private Condition condition = null;

    /** Applies a filter to the query, manifesting as a WHERE clause in the SQL statement.
     * If the query already has a filter, both the original filter and this filter will be applied using an AND operator.
     * @param conditionFn A function receiving the context of the current table and returning the condition to apply
     */
    public SelectQueryBuilder filter(Function<TableContext, Condition> conditionFn) {
        var condition = conditionFn.apply(new TableContext(table));
        if (condition == null) {
            throw new IllegalArgumentException("Condition can't be null");
        }
        if (this.condition == null) {
            this.condition = condition;
        } else {
             this.condition = this.condition.and(condition);
        }
         return this;
    }

    /** Only applies the given function to the query builder if the given condition is true.
     *
     * @param condition Whether to apply the function
     * @param thenFn A function that modifies the query builder and returns it
     * @return The modified query builder if the condition is true, otherwise the original query builder
     */
    public SelectQueryBuilder when(boolean condition, UnaryOperator<SelectQueryBuilder> thenFn) {
        if (condition) {
            return thenFn.apply(this);
        } else {
            return this;
        }
    }

    /** Orders the results of the query by the given sort order */
    public SelectQueryBuilder orderBy(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    private String asSql() {
        var sb = new StringBuilder();
        sb.append("SELECT");
        if (columns.isEmpty()) {
            sb.append(" * ");
        } else {
            sb.append(" ");
            sb.append(columns.stream().map(BuilderUtils::quoteIdentifier).collect(Collectors.joining(", ")));
            sb.append(" ");
        }
        sb.append("FROM ").append(BuilderUtils.quoteIdentifier(table));
        if (condition != null) {
            sb.append(" WHERE ").append(condition.asParameterizedSql());
        }
        if (sortOrder != null) {
            sb.append(" ORDER BY ");
            var sortColumns = sortOrder.entries().stream()
                    .map(e -> BuilderUtils.quoteIdentifier(e.column()) + " " + e.order())
                    .collect(Collectors.joining(", "));
            sb.append(sortColumns);
        }
        return sb.toString();
    }

    private int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        if (condition != null) {
             return condition.fillParameters(statement, i);
        }
        return i;
    }

    /** Prepares the query for execution by building the SQL string */
    public PreparedSelectQuery prepare() throws DatabaseException {
        var statement = controller.prepareStatement(asSql());
        return new PreparedSelectQuery(statement, this::fillParameters);
    }
}
