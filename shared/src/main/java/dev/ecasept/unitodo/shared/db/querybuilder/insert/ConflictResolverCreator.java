package dev.ecasept.unitodo.shared.db.querybuilder.insert;

import dev.ecasept.unitodo.shared.db.querybuilder.expressions.TableContext;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConflictResolverCreator {
    private final Function<BiConsumer<ConflictResolver, TableContext>, InsertQueryBuilder> callback;
    public ConflictResolverCreator(Function<BiConsumer<ConflictResolver, TableContext>, InsertQueryBuilder> callback) {
        this.callback = callback;
    }
    public InsertQueryBuilder doUpdate(BiConsumer<ConflictResolver, TableContext> resolverInit) {
        return callback.apply(resolverInit);
    }

    public InsertQueryBuilder doNothing() {
        return callback.apply((resolver, t) -> {});
    }
}
