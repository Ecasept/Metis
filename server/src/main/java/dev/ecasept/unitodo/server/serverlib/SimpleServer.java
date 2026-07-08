package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.models.api.ErrorCode;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.serialization.adapters.Any;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.utils.Log;

/** Hosts a simple HTTP or HTTPS server with routes that can be added at runtime. */
public class SimpleServer {
    private static final String TAG = "SimpleHttpsServer";
    private final HttpServer server;
    private final HashMap<RouteKey, Route<?, ?>> routes = new HashMap<>();
    private final Serializer defaultSerializer = Serializer.createDefault().adapter(ApiResponseAdapter<Any>::new, new StoreType<>(){});

    /** Creates a new server
     *
     * @param keystorePassword The password of the ssl certificate, if using HTTPS
     * @param keystoreLocation The location of the ssl certificate, if using HTTPS
     * @param useHttps Whether to even use HTTPS or not
     */
    public SimpleServer(String keystorePassword, String keystoreLocation, boolean useHttps) {
        if (useHttps) {
            HttpsServer server;
            try {
                server = HttpsServer.create(null, 0);
            } catch (IOException e) {
                throw new RuntimeException("Error creating HttpsServer", e);
            }
            server.setHttpsConfigurator(HttpsConfiguratorFactory.create(keystorePassword, keystoreLocation));
            this.server = server;
        } else {
            try {
                this.server = HttpServer.create(null, 0);
            } catch (IOException e) {
                throw new RuntimeException("Error creating HttpServer", e);
            }
        }
        this.server.createContext("/", this::handleRequest);
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
                sendApiError(exchange, 400, "Bad Request: Invalid or missing request path", defaultSerializer);
                return;
            }
            path = normalizePath(path);
            var method = exchange.getRequestMethod();
            var key = new RouteKey(path, method);
            var route = routes.get(key);
            if (route == null) {
                    sendApiError(exchange, 404, "The requested URL " + path + " was not found on this server.", defaultSerializer);
                return;
            }
            route.handle(exchange, this);
        } catch (Exception e) {
            Log.e(TAG, "Error handling request", e);
            sendApiError(exchange, 500, "Internal server error", defaultSerializer);
        }
    }

    /** Returns an {@link ErrorCode#UNKNOWN} ApiError to the client
     *
     * @param exchange The exchange to use to send the request
     * @param responseCode The HTTP response code to send
     * @param errorMsg The message to include
     * @param serializer The serializer to use to serialize the response
     */
    public void sendApiError(HttpExchange exchange, int responseCode, String errorMsg, Serializer serializer) {
        ApiResponse<Void> response = ApiResponse.error(errorMsg, ErrorCode.UNKNOWN);
        byte[] rawResponseBody;
        try {
            rawResponseBody = serializer.serialize(response, new StoreType<>(){});
        } catch (Exception e) {
            Log.e(TAG, "Failed to serialize API error response", e);
            sendError(exchange, 500, "Internal server error");
            return;
        }
        try {
            exchange.sendResponseHeaders(responseCode, rawResponseBody.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(rawResponseBody);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to send API error response", e);
        }
    }

    /** Sends a plain text error response to the client
     *
     * @param exchange The exchange to use to send the request
     * @param responseCode The HTTP response code to send
     * @param msg The message to include
     */
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


    /** Adds a new route handler for a specific route and method.
     * The function will be called when the server receives a request for that specific url and method, and is expected to handle the request and return an approriate response.
     *
     * @param route The route to handle
     * @param method Which method the route should accept
     * @param requestType The type of the request body, as a {@link StoreType}
     * @param responseType The type of the response body, as a {@link StoreType}
     * @param func The actual handler that will be called when the route is hit
     * @param <RequestType> The type of the request body
     * @param <ResponseType> The type of the response body
     */
    public <RequestType, ResponseType> void addRoute(String route, String method, StoreType<RequestType> requestType, StoreType<ResponseType> responseType, RouteHandler<RequestType, ResponseType> func) {
        routes.put(new RouteKey(route, method), new Route<>(requestType, responseType, func));
    }

    /** Starts the server on the specified port */
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
