package dev.ecasept.unitodo.shared.db.querybuilder;

public class BuilderUtils {
    public static String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
