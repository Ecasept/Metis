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

/** Dispatches API calls for this application */
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
    /** Sets the session token that is included in necessary requests */
    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    /** Sends credentials to the server and receives a session token.
     *
     * @param username The username to send to the server
     * @param password The password to send to the server
     * @return The session token received from the server if the login was successful
     * @throws ApiException If the login was unsuccessful or any other error occurred
     */
    public String login(String username, Password password) throws ApiException {
        var requestBody = new UsernameAndPassword(username, password);
        return dispatch(() -> sendPost("/auth/login", new StoreType<>() {}, new StoreType<>() {}, requestBody, false));
    }

    /** Sends credentials to the server to create a new account and receives a session token.
     *
     * @param username The username to send to the server
     * @param password The password to send to the server
     * @return The session token received from the server if the registration was successful
     * @throws ApiException If the registration was unsuccessful or any other error occurred
     */
    public String register(String username, Password password) throws ApiException {
        var requestBody = new UsernameAndPassword(username, password);
        return dispatch(() -> sendPost("/auth/register", new StoreType<>() {}, new StoreType<>() {}, requestBody, false));
    }

    /** Sends a request to the server to delete the account associated with the current session token.
     *
     * @param password The password to send to the server for verification
     * @throws ApiException If the deletion was unsuccessful or any other error occurred
     */
    public void deleteAccount(Password password) throws ApiException {
        dispatch(() -> sendPost("/users/delete", new StoreType<ApiResponse<Void>>() {}, new StoreType<>() {}, password, true));
    }

    /** Sends a request to the server to synchronize data between the client and server.
     *
     * @param request The synchronization request containing the data to be synchronized
     * @return The synchronization response containing the new data from the server
     * @throws ApiException If the server-side synchronization was unsuccessful or any other error occurred
     */
    public SyncResponse sync(SyncRequest request) throws ApiException {
        return dispatch(() -> sendPost("/data/sync", new StoreType<>() {}, new StoreType<>() {}, request, true));
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

    private <RequestType, ResponseType> ResponseType sendPost(String endpoint, StoreType<ResponseType> responseType, StoreType<RequestType> requestType, RequestType requestBody, boolean authorized) throws SerializationException, ApiNetworkException {
        return sendRequest(endpoint, responseType, req -> req.POST(HttpRequest.BodyPublishers.ofByteArray(serializer.serialize(requestBody, requestType))), authorized);
    }

    private <ResponseType> ResponseType sendRequest(String endpoint, StoreType<ResponseType> responseType, UnaryOperator<HttpRequest.Builder> method, boolean authorized) throws ApiNetworkException, SerializationException {
        var builder = method.apply(HttpRequest.newBuilder()
                .uri(java.net.URI.create(baseUrl + endpoint)));
        var sessionToken = getSessionToken();
        if (authorized) {
            if (sessionToken.isEmpty()) {
                throw new IllegalArgumentException("sessionToken must be present for authorized calls");
            } else {
                builder.header("Authorization", "Bearer " + sessionToken.get());
            }
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