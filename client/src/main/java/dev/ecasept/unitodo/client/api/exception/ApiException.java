package dev.ecasept.unitodo.client.api.exception;

public class ApiException extends Exception {
    public ApiException(String message) {
        super(message);
    }
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
    public ApiException(Throwable cause) {
        super(cause);
    }
    public ApiException() {
        super();
    }}
