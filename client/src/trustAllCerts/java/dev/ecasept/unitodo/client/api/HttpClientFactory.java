package dev.ecasept.unitodo.client.api;

import dev.ecasept.unitodo.shared.utils.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class HttpClientFactory {
    private static final String TAG = "HttpClientFactory";
    public static HttpClient createClient() {
        Log.w(TAG, "Creating HttpClient that trusts all certificates. This is insecure and should only be used for testing purposes.");
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing SSLContext", e);
        }
        try {
            sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (KeyManagementException e) {
            throw new RuntimeException("Error initializing SSLContext with trust-all TrustManager", e);
        }

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }
}
