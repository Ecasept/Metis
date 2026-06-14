package dev.ecasept.unitodo.client.api;

import dev.ecasept.unitodo.client.api.exception.ApiNetworkException;
import dev.ecasept.unitodo.shared.serialization.SerializationException;

@FunctionalInterface
public interface ApiCall<T> {
    T get() throws ApiNetworkException, SerializationException;
}
