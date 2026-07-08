package dev.ecasept.unitodo.server.serverlib;

import java.util.HashMap;

/** Contains the data for a response
 *
 * @param code The response code to return to the client
 * @param body The data to return
 * @param headers Any headers to include
 * @param <T> The type of the response body
 */
public record Response<T>(int code, T body, HashMap<String, String> headers) {
    public Response(int code, T body) {
        this(code, body, new HashMap<>());
    }
}
