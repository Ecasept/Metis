package dev.ecasept.unitodo.shared.db.querybuilder.insert;

sealed interface ConflictResolveOperation permits ConflictResolveOperation.Copy, ConflictResolveOperation.Set {
    record Copy() implements ConflictResolveOperation {}
    record Set(String expression) implements ConflictResolveOperation {}
}
