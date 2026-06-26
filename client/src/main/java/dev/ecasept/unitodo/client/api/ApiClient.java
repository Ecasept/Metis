package dev.ecasept.unitodo.client.api;

import dev.ecasept.unitodo.client.api.exception.ApiException;
import dev.ecasept.unitodo.client.api.exception.ApiSerializationException;
import dev.ecasept.unitodo.client.api.exception.ApiNetworkException;
import dev.ecasept.unitodo.client.api.exception.ApiServerErrorException;
import dev.ecasept.unitodo.shared.models.api.*;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.utils.Log;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private final HttpClient httpClient;
    private final String baseUrl;
    private final Serializer serializer;
    public ApiClient(HttpClient httpClient, String baseUrl, Serializer serializer) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.serializer = serializer;
        Log.i(TAG, "ApiClient initialized with base URL: " + baseUrl);
    }

    private String sessionToken;
    private Optional<String> getSessionToken() {
        return Optional.ofNullable(sessionToken);
    }
    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    public String login(String username, String password) throws ApiException {
        var requestBody = new UsernameAndPassword(username, new Password(password));
        return dispatch(() -> sendPost("/auth/login", new StoreType<>() {}, requestBody, false));
    }
    public String register(String username, String password) throws ApiException {
        var requestBody = new UsernameAndPassword(username, new Password(password));
        return dispatch(() -> sendPost("/auth/register", new StoreType<>() {}, requestBody, false));
    }
    public void deleteAccount(String password) throws ApiException {
        try (var requestBody = new Password(password)) {
            dispatch(() -> sendPost("/users/delete", new StoreType<>() {}, requestBody, true));
        }
    }
    public SyncResponse sync(SyncRequest request) throws ApiException {
        return dispatch(() -> sendPost("/data/sync", new StoreType<>() {}, request, true));
    }

    private <T> T dispatch(ApiCall<ApiResponse<T>> apiCall) throws ApiException {
        try {
            return apiCall.get().on(
                    data -> data,
                    error -> {
                        Log.e(TAG, "API call failed: " + error);
                        throw new ApiServerErrorException("API call failed: " + error);
                    }
            );
        } catch (SerializationException e) {
            throw new ApiSerializationException("Failed to serialize/deserialize API data", e);
        }
    }

    private <RequestType, ResponseType> ResponseType sendPost(String endpoint, StoreType<ResponseType> responseType, RequestType requestBody, boolean authorized) throws SerializationException, ApiNetworkException {
        return sendRequest(endpoint, responseType, req -> req.POST(HttpRequest.BodyPublishers.ofByteArray(serializer.serialize(requestBody))), authorized);
    }

    private <ResponseType> ResponseType sendRequest(String endpoint, StoreType<ResponseType> responseType, UnaryOperator<HttpRequest.Builder> method, boolean authorized) throws ApiNetworkException, SerializationException {
        var builder = method.apply(HttpRequest.newBuilder()
                .uri(java.net.URI.create(baseUrl + endpoint)));
        var sessionToken = getSessionToken();
        if (authorized && sessionToken.isPresent()) {
            builder.header("Authorization", "Bearer " + sessionToken.get());
        }
        var request = builder.build();
        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            Log.i(TAG, "Request to " + endpoint + " returned status code: " + response.statusCode());
        } catch (IOException | InterruptedException e) {
            throw new ApiNetworkException("Failed to send request to " + endpoint, e);
        }

        try {
            return serializer.deserialize(response.body(), responseType);
        } catch (SerializationException e) {
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiNetworkException("Server returned non-200 status code: " + response.statusCode() + " and raw body could not be parsed.", e);
            }
            throw e;
        }
    }
}