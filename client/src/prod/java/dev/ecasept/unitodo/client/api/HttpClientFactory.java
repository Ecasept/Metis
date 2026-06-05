package dev.ecasept.unitodo.client.api;

import java.net.http.HttpClient;

public class HttpClientFactory {
    public static HttpClient createClient() {
        return HttpClient.newHttpClient();
    }
}
