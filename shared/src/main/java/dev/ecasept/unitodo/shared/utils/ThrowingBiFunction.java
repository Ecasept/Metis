package dev.ecasept.unitodo.shared.utils;

/**
 * Implementation of {@link java.util.function.BiFunction} that allows exceptions to be thrown
 * @param <T> Type of the first input parameter
 * @param <U> Type of the second input parameter
 * @param <R> Type of the return value
 * @param <E> Type of the exception that can be thrown
 */
@FunctionalInterface
public interface ThrowingBiFunction<T, U, R, E extends Throwable> {
    /** Process the input and return a value or throw an exception
     * @param t First input parameter
     * @param u Second input parameter
     * @return Return value
     * @throws E Exception that can be thrown
     */
    R apply(T t, U u) throws E;
}
