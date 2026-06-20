package dev.ecasept.unitodo.shared.db.querybuilder.select;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import dev.ecasept.unitodo.shared.db.querybuilder.SortOrder;
import dev.ecasept.unitodo.shared.db.querybuilder.conditions.Condition;
import dev.ecasept.unitodo.shared.db.querybuilder.conditions.ConditionCreator;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    public SelectQueryBuilder filter(Consumer<ConditionCreator> conditionFn) {
        var conditionCreator = new ConditionCreator();
        conditionFn.accept(conditionCreator);
        if (conditionCreator.getCondition() == null) {
            throw new IllegalArgumentException("Condition can't be null");
        }
        this.condition = conditionCreator.getCondition();
        return this;
    }

    public SelectQueryBuilder when(boolean condition, UnaryOperator<SelectQueryBuilder> thenFn) {
        if (condition) {
            return thenFn.apply(this);
        } else {
            return this;
        }
    }

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
            sb.append(" ORDER BY ").append(BuilderUtils.quoteIdentifier(sortOrder.column())).append(" ").append(sortOrder.orderAsSql());
        }
        return sb.toString();
    }

    private int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        if (condition != null) {
             return condition.fillParameters(statement, i);
        }
        return i;
    }

    public PreparedSelectQuery prepare() throws DatabaseException {
        var statement = controller.prepareStatement(asSql());
        return new PreparedSelectQuery(statement, this::fillParameters);
    }
}
