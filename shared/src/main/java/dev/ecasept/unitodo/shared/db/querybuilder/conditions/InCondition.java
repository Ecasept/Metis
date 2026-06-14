package dev.ecasept.unitodo.shared.db.querybuilder.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class InCondition<T> implements Condition {
    private final String column;
    private final List<T> options;

    public InCondition(String column, List<T> options) {
        this.column = column;
        this.options = options;
    }

    public String asParameterizedSql() {
        return "(" + BuilderUtils.quoteIdentifier(column) + " IN (" + "?, ".repeat(options.size() - 1) + "?))";
    }

    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
        try {
            for (var option : options) {
                statement.setObject(i, option);
                i++;
            }
            return i;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to fill parameters for InCondition", e);
        }
    }
}
