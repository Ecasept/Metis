package dev.ecasept.unitodo.shared.utils;

@FunctionalInterface
public interface ThrowableFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;
}
