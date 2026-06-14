package dev.ecasept.unitodo.shared.db.querybuilder.insert;

import dev.ecasept.unitodo.shared.db.querybuilder.BuilderUtils;
import java.util.LinkedHashMap;
import java.util.List;

public class ConflictResolver {
    private LinkedHashMap<String, ConflictResolveOperation> ops = new LinkedHashMap<>();
    private final List<String> keys;

    public ConflictResolver(List<String> keys) {
        this.keys = keys;
    }

    public void nothing() {
        ops = null;
    }

    public ConflictResolver copy(String... columns) {
        if (ops != null) {
            for (String column : columns) {
                ops.put(column, new ConflictResolveOperation.Copy());
            }
        }
        return this;
    }

    public ConflictResolver set(String column, String expression) {
        if (ops != null) ops.put(column, new ConflictResolveOperation.Set(expression));
        return this;
    }

    public StringBuilder asSql() {
        var sb = new StringBuilder();
        sb.append("ON CONFLICT");
        if (!keys.isEmpty()) {
            sb.append(" (");
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(BuilderUtils.quoteIdentifier(keys.get(i)));
            }
            sb.append(")");
        }
        
        if (ops == null) {
            sb.append("\nDO NOTHING");
        } else {
            if (ops.isEmpty()) {
                throw new IllegalStateException("Conflict resolver requires at least one operation for DO UPDATE SET, or call nothing()");
            }
            sb.append("\nDO UPDATE SET\n");
            int i = 0;
            for (var entry : ops.entrySet()) {
                if (i > 0) sb.append(",\n");
                sb.append("    ").append(BuilderUtils.quoteIdentifier(entry.getKey())).append(" = ");
                switch (entry.getValue()) {
                    case ConflictResolveOperation.Copy c -> sb.append("EXCLUDED.").append(BuilderUtils.quoteIdentifier(entry.getKey()));
                    case ConflictResolveOperation.Set set -> sb.append(set.expression());
                }
                i++;
            }
        }
        return sb;
    }
}
