package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.Headers;
import dev.ecasept.unitodo.shared.db.DatabaseException;

@FunctionalInterface
public interface RouteHandler<RequestType, ResponseType> {
    Response<ResponseType> handle(RequestType request, Headers headers) throws DatabaseException;
}
