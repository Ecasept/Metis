package dev.ecasept.unitodo.shared.db.querybuilder.conditions;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LikeCondition implements Condition {
    private final String column;
    private final String pattern;
    public LikeCondition(String column, String pattern) {
        this.column = column;
        this.pattern = pattern;
    }
    public String asParameterizedSql() {
        return "(" + BuilderUtils.quoteIdentifier(column) + " LIKE ?)";
    }
    public int fillParameters(PreparedStatement statement, int i) throws DatabaseException {
         try {
             statement.setString(i, pattern);
             return i + 1;
         } catch (SQLException e) {
             throw new DatabaseException("Failed to fill parameters for LikeCondition", e);
         }
    }
}
