package dev.ecasept.unitodo.shared.db.querybuilder.select;

import java.sql.SQLException;

@FunctionalInterface
public interface Converter<From, To> {
    To convert(From from) throws SQLException;
}
