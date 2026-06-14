package dev.ecasept.unitodo.shared.db.querybuilder.insert;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConflictResolverCreator {
    private final Function<Consumer<ConflictResolver>, InsertQueryBuilder> callback;
    public ConflictResolverCreator(Function<Consumer<ConflictResolver>, InsertQueryBuilder> callback) {
        this.callback = callback;
    }
    public InsertQueryBuilder doUpdate(Consumer<ConflictResolver> resolverInit) {
        return callback.apply(resolverInit);
    }

    public InsertQueryBuilder doNothing() {
        return callback.apply(resolver -> {});
    }
}
