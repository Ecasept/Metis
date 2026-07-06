package dev.ecasept.unitodo.client.api;

import dev.ecasept.unitodo.client.api.exception.ApiNetworkException;
import dev.ecasept.unitodo.shared.serialization.SerializationException;

/** Represents an API call
 * @param <T> The type of the response
 */
@FunctionalInterface
public interface ApiCall<T> {
    /** Executes the API call and returns the response, or throws an error */
    T get() throws ApiNetworkException, SerializationException;
}
