package dev.ecasept.unitodo.shared.db.querybuilder.delete;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.TableContext;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions.Condition;

import java.sql.PreparedStatement;
import java.util.function.Function;

public class DeleteQueryBuilder {
    private final DatabaseController controller;
    private final String table;
    public DeleteQueryBuilder(DatabaseController controller, String table) {
        this.controller = controller;
        this.table = table;
    }
    private Condition condition = null;

    public DeleteQueryBuilder filter(Function<TableContext, Condition> conditionFn) {
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

    private String asSql() {
        var sb = new StringBuilder();
        sb.append("DELETE FROM ").append(BuilderUtils.quoteIdentifier(table));
        if (condition != null) {
            sb.append(" WHERE ").append(condition.asParameterizedSql());
        }
        return sb.toString();
    }

    private int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        if (condition != null) {
             return condition.fillParameters(statement, i);
        }
        return i;
    }

    public PreparedDeleteQuery prepare() throws DatabaseException {
        var statement = controller.prepareStatement(asSql());
        return new PreparedDeleteQuery(statement, this::fillParameters);
    }
}
