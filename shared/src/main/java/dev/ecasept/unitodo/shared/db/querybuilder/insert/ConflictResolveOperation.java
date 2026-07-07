package dev.ecasept.unitodo.shared.db.querybuilder.insert;

import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;

sealed interface ConflictResolveOperation permits ConflictResolveOperation.Copy, ConflictResolveOperation.Set {
    record Copy() implements ConflictResolveOperation {}
    record Set(SqlExpression expression) implements ConflictResolveOperation {}
}
