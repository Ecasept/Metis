package dev.ecasept.unitodo.server.serverlib;

public class Response<T> {
    public T body;
    public int code;

    public Response(int code, T body) {
        this.code = code;
        this.body = body;
    }
}
