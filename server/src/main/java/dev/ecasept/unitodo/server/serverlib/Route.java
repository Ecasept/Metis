package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.HttpExchange;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.serialization.Serializer;

import java.io.IOException;
import java.io.OutputStream;

public record Route<RequestType, ResponseType>(StoreType<RequestType> requestType, StoreType<ResponseType> responseType, RouteHandler<RequestType, ResponseType> func) {
    private static final String TAG = "RouteValue";
    private static final Serializer serializer = Serializer.createDefault().adapter(ApiResponseAdapter.class, ApiResponse.class);

    public void handle(HttpExchange exchange, SimpleHttpsServer server) throws IOException, DatabaseException {
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
            var rawResponseBody = serializer.serialize(response.body());
            exchange.sendResponseHeaders(response.code(), rawResponseBody.length);
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(rawResponseBody);
            }
    }
}
