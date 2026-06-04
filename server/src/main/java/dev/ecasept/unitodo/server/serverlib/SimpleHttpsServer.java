package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.function.Function;

import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.server.Configuration;
import dev.ecasept.unitodo.shared.utils.Log;

public class SimpleHttpsServer {
    private static final String TAG = "SimpleHttpsServer";
    private final HttpsServer server;
    private final HashMap<RouteKey, Route<?, ?>> routes = new HashMap<>();

    public SimpleHttpsServer(String keystorePassword, String keystoreLocation) {
        char[] pw = keystorePassword.toCharArray();
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Error initializing keystore: " + keystoreLocation, e);
        }
        try (FileInputStream fis = new FileInputStream(keystoreLocation)) {
            ks.load(fis, pw);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Keystore file not found: " + keystoreLocation, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading keystore file: " + keystoreLocation, e);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException("Error loading keystore: " + keystoreLocation, e);
        }

        KeyManagerFactory kmf;
        try {
            kmf = KeyManagerFactory.getInstance("SunX509");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing KeyManagerFactory", e);
        }
        try {
            kmf.init(ks, pw);
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing KeyManagerFactory with keystore: " + keystoreLocation, e);
        }

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing SSLContext", e);
        }
        try {
            sslContext.init(kmf.getKeyManagers(), null, null);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Error initializing SSLContext with KeyManagerFactory", e);
        }

        HttpsServer server;
        try {
            server = HttpsServer.create(null, 0);
        } catch (IOException e) {
            throw new RuntimeException("Error creating HttpsServer on port " + Configuration.PORT, e);
        }
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                SSLParameters sslParams = sslContext.getDefaultSSLParameters();
                params.setSSLParameters(sslParams);
            }
        });
        this.server = server;
        server.createContext("/", this::handleRequest);
    }

    private String normalizePath(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private void handleRequest(HttpExchange exchange) {
        try {
            URI uri = exchange.getRequestURI();
            Log.i(TAG, "Received request for URI: " + uri);
            var path = uri.getPath();
            if (path == null || path.isEmpty()) {
                sendError(exchange, 400, "Invalid path");
                return;
            }
            path = normalizePath(path);
            var method = exchange.getRequestMethod();
            var key = new RouteKey(path, method);
            var route = routes.get(key);
            if (route == null) {
                sendError(exchange, 404, "The requested URL " + path + " was not found on this server.");
                return;
            }
            route.handle(exchange, this);
        } catch (Exception e) {
            Log.e(TAG, "Error handling request", e);
            sendError(exchange, 500, "Internal server error");
        }
    }

    public void sendError(HttpExchange exchange, int responseCode, String msg) {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(responseCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to send error response", e);
        }
    }


    public <RequestType, ResponseType, StoredRequestType extends StoreType<RequestType>, StoredResponseType extends StoreType<ResponseType>> void addRoute(String route, String method, StoredRequestType requestType, StoredResponseType responseType, Function<RequestType, Response<ResponseType>> func) {
        routes.put(new RouteKey(route, method), new Route<>(requestType, responseType, func));
    }

    public void run(int port) {
        try {
            server.bind(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException("Error binding server to port " + port, e);
        }
        server.setExecutor(null);
        server.start();
        Log.i(TAG, "HTTPS server started on port " + port);
    }
}
