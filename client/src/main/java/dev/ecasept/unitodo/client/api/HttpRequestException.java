package dev.ecasept.unitodo.client.api;

public class HttpRequestException extends Exception {
    public HttpRequestException(String message) {
        super(message);
    }
    public HttpRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    public HttpRequestException(Throwable cause) {
        super(cause);
    }
    public HttpRequestException() {
        super();
    }
}

