package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.HttpClientFactory;
import dev.ecasept.unitodo.shared.models.ApiResponse;
import dev.ecasept.unitodo.shared.models.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.utils.Log;


public class Main {
    public static void main(String[] args) {
        var serializer = Serializer.createDefault().adapter(ApiResponseAdapter.class, ApiResponse.class);
        var httpClient = HttpClientFactory.createClient();
        var apiClient = new ApiClient(httpClient, "https://localhost:6767/api", serializer);

        // Example
        var res = apiClient.login("testuser", "password123");
        res.on(
                sessionToken -> {
                    Log.i("Main", "Login successful! Session token: " + sessionToken);
                    // Save session token
                },
                errorMessage -> {
                    Log.e("Main", "Login failed: " + errorMessage);
                }
        );
    }
}
