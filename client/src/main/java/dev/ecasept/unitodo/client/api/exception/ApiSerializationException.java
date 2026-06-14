package dev.ecasept.unitodo.client.api.exception;

public class ApiSerializationException extends ApiException {
    public ApiSerializationException(String message) {
        super(message);
    }
    public ApiSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
    public ApiSerializationException(Throwable cause) {
        super(cause);
    }
    public ApiSerializationException() {
        super();
    }}
