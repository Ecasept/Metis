package dev.ecasept.unitodo.shared.db.querybuilder.delete;

import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import dev.ecasept.unitodo.shared.db.querybuilder.conditions.Condition;
import dev.ecasept.unitodo.shared.db.querybuilder.conditions.ConditionCreator;

import java.sql.PreparedStatement;
import java.util.function.Consumer;

public class DeleteQueryBuilder {
    private final DatabaseController controller;
    private final String table;
    public DeleteQueryBuilder(DatabaseController controller, String table) {
        this.controller = controller;
        this.table = table;
    }
    private Condition condition = null;

    public DeleteQueryBuilder filter(Consumer<ConditionCreator> conditionFn) {
        var conditionCreator = new ConditionCreator();
        conditionFn.accept(conditionCreator);
        if (conditionCreator.getCondition() == null) {
            throw new IllegalArgumentException("Condition can't be null");
        }
        this.condition = conditionCreator.getCondition();
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
