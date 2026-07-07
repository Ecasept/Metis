package dev.ecasept.unitodo.shared.utils;

/** A supplier that can throw two different types of exceptions. */
@FunctionalInterface
public interface ThrowingSupplier2<T, E1 extends Throwable, E2 extends Throwable> {
    T get() throws E1, E2;
}
