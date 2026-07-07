package dev.ecasept.unitodo.shared.utils;


/**
 * Implementation of {@link java.util.function.Function} that allows exceptions to be thrown
 * @param <T> Type of the input parameter
 * @param <R> Type of the return value
 * @param <E> Type of the exception that can be thrown
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Throwable> {
    /** Process the input parameter and return a value, or throw an exception
     * @param t Input parameter
     * @return Return value
     * @throws E Exception that can be thrown
     */
    R apply(T t) throws E;
}
