package dev.ecasept.unitodo.shared.db.querybuilder;

import dev.ecasept.unitodo.shared.db.querybuilder.batch.BatcherPlaceholder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BuilderUtils {
    public static String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    public static void bindParameter(PreparedStatement statement, int index, Object value) throws SQLException {
        if (value instanceof BatcherPlaceholder placeholder) {
            value = placeholder.obj;
        }
        if (value == null) {
            statement.setNull(index, Types.NULL);
        } else {
            statement.setObject(index, value);
        }
    }
}
