package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.HttpExchange;
import dev.ecasept.unitodo.shared.serialization.RawData;
import dev.ecasept.unitodo.shared.serialization.types.StoreType;
import dev.ecasept.unitodo.shared.serialization.adapters.RawDataAdapter;
import dev.ecasept.unitodo.shared.serialization.adapters.LocalDateTimeAdapter;
import dev.ecasept.unitodo.shared.serialization.Serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.function.Function;

public record Route<RequestType, ResponseType, StoredRequestType extends StoreType<RequestType>, StoredResponseType extends StoreType<ResponseType>>(StoredRequestType requestType, StoredResponseType responseType, Function<RequestType, Response<ResponseType>> func) {
    private static final String TAG = "RouteValue";
    public void handle(HttpExchange exchange) throws IOException {
            var rawRequestBody = exchange.getRequestBody();
            var bytes = rawRequestBody.readAllBytes();
            var s = new Serializer()
                    .adapter(LocalDateTimeAdapter.class, LocalDateTime.class)
                    .adapter(RawDataAdapter.class, RawData.class);
            var requestBody = s.deserialize(bytes, requestType);
            var response = func.apply(requestBody);
            var rawResponseBody = s.serialize(response.body());
            exchange.sendResponseHeaders(response.code(), rawResponseBody.length);
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(rawResponseBody);
            }
    }
}
