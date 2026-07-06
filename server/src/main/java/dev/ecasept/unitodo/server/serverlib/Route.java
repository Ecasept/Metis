package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.HttpExchange;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.adapters.Any;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.io.IOException;
import java.io.OutputStream;

public record Route<RequestType, ResponseType>(StoreType<RequestType> requestType, StoreType<ResponseType> responseType, RouteHandler<RequestType, ResponseType> func) {
    private static final String TAG = "RouteValue";
    private static final Serializer serializer = Serializer.createDefault().adapter(ApiResponseAdapter<Any>::new, new StoreType<>(){});

    public void handle(HttpExchange exchange, SimpleServer server) throws IOException, DatabaseException {
        var rawRequestBody = exchange.getRequestBody();
        var bytes = rawRequestBody.readAllBytes();
        RequestType requestBody;
        try {
            requestBody = serializer.deserialize(bytes, requestType);
        } catch (SerializationException e) {
            server.sendApiError(exchange, 400, "Bad Request: Invalid request body", serializer);
            return;
        }
        var response = func.handle(requestBody, exchange.getRequestHeaders());
        var rawResponseBody = serializer.serialize(response.body(), responseType);
        exchange.sendResponseHeaders(response.code(), rawResponseBody.length);
        try(OutputStream os = exchange.getResponseBody()) {
            os.write(rawResponseBody);
        }
        Log.i(TAG, "Handled request to " + exchange.getRequestURI() + " with response code " + response.code());
    }
}
