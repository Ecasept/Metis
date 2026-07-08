package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.Headers;
import dev.ecasept.unitodo.shared.db.DatabaseException;

/** A function that can handle a request to a route
 *
 * @param <RequestType> The type of data the route can recieve and handle
 * @param <ResponseType> The type of data the route will return in response to a request
 */
@FunctionalInterface
public interface RouteHandler<RequestType, ResponseType> {
    /** Handles a request to a route appropriately and returns a response.
     *
     * @param request The actual request data sent to the route
     * @param headers Any headers included in the request
     * @return A response containing the data to be sent back to the client
     * @throws DatabaseException If any database accesses fail during the handling, will be returns as a 500 Internal Server Error to the client
     */
    Response<ResponseType> handle(RequestType request, Headers headers) throws DatabaseException;
}
