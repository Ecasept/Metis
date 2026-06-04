package dev.ecasept.unitodo.client.api;

import dev.ecasept.unitodo.shared.models.ApiResponse;
import dev.ecasept.unitodo.shared.models.Password;
import dev.ecasept.unitodo.shared.models.UsernameAndPassword;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.utils.Log;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    }

    public ApiResponse<String> login(String username, String password) {
        var requestBody = new UsernameAndPassword(username, new Password(password));
        return dispatch(() -> sendPost("/auth/login", new StoreType<>() {}, requestBody));
    }
    public ApiResponse<String> register(String username, String password) {
        var requestBody = new UsernameAndPassword(username, new Password(password));
        return dispatch(() -> sendPost("/auth/register", new StoreType<>() {}, requestBody));
    }
    public ApiResponse<Void> deleteAccount(String username, String password) {
        var requestBody = new UsernameAndPassword(username, new Password(password));
        return dispatch(() -> sendDelete("/auth/register", new StoreType<>() {}, requestBody));
    }

    private <T> ApiResponse<T> dispatch(ApiCall<ApiResponse<T>> apiCall) {
        try {
            return apiCall.get();
        } catch (HttpRequestException e) {
            Log.e(TAG, "HTTP request failed: " + e.getMessage(), e);
            return ApiResponse.error("Failed to connect to server: " + e.getMessage());
        } catch (SerializationException e) {
            Log.e(TAG, "Failed to parse server response: " + e.getMessage(), e);
            return ApiResponse.error("Failed to parse server response: " + e.getMessage());
        }
    }

    private <RequestType, ResponseType> ResponseType sendPost(String endpoint, StoreType<ResponseType> responseType, RequestType requestBody) throws HttpRequestException, SerializationException {
        return sendRequest(endpoint, responseType, req -> req.POST(HttpRequest.BodyPublishers.ofByteArray(serializer.serialize(requestBody))));
    }
    private <RequestType, ResponseType> ResponseType sendDelete(String endpoint, StoreType<ResponseType> responseType, RequestType requestBody) throws HttpRequestException, SerializationException {
        return sendRequest(endpoint, responseType, req -> req.method("DELETE", HttpRequest.BodyPublishers.ofByteArray(serializer.serialize(requestBody))));
    }

    private <ResponseType> ResponseType sendRequest(String endpoint, StoreType<ResponseType> responseType, UnaryOperator<HttpRequest.Builder> method) throws HttpRequestException, SerializationException {
        HttpRequest request = method.apply(HttpRequest.newBuilder()
                .uri(java.net.URI.create(baseUrl + endpoint)))
                .build();
        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException | InterruptedException e) {
            throw new HttpRequestException("Failed to send request to " + endpoint, e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new HttpRequestException("Received non-200 response: " + response.statusCode() + " with body: " + new String(response.body()));
        }
        return serializer.deserialize(response.body(), responseType);
    }
}