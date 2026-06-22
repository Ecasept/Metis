package dev.ecasept.unitodo.server.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record User (UUID userId, String username, String passwordHash) {
    public static User fromResultSet(ResultSet rs) throws SQLException {
        return new User(
                UUID.fromString(rs.getString("useId")),
                rs.getString("username"),
                rs.getString("password_hash")
        );
    }
}
