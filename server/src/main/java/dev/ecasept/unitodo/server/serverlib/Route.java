package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.HttpExchange;
import dev.ecasept.unitodo.shared.models.ApiResponse;
import dev.ecasept.unitodo.shared.models.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.SerializationException;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.serialization.adapters.RawDataAdapter;
import dev.ecasept.unitodo.shared.serialization.adapters.LocalDateTimeAdapter;
import dev.ecasept.unitodo.shared.serialization.Serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.function.Function;

public record Route<RequestType, ResponseType>(StoreType<RequestType> requestType, StoreType<ResponseType> responseType, Function<RequestType, Response<ResponseType>> func) {
    private static final String TAG = "RouteValue";
    private static final Serializer serializer = Serializer.createDefault().adapter(ApiResponseAdapter.class, ApiResponse.class);

    public void handle(HttpExchange exchange, SimpleHttpsServer server) throws IOException {
            var rawRequestBody = exchange.getRequestBody();
            var bytes = rawRequestBody.readAllBytes();
            RequestType requestBody;
            try {
                requestBody = serializer.deserialize(bytes, requestType);
            } catch (SerializationException e) {
                server.sendError(exchange, 400, "Invalid request body");
                return;
            }
            var response = func.apply(requestBody);
            var rawResponseBody = serializer.serialize(response.body());
            exchange.sendResponseHeaders(response.code(), rawResponseBody.length);
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(rawResponseBody);
            }
    }
}
