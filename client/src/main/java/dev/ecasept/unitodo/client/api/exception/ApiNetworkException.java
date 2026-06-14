package dev.ecasept.unitodo.client.api.exception;

public class ApiNetworkException extends ApiException {
    public ApiNetworkException(String message) {
        super(message);
    }
    public ApiNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
    public ApiNetworkException(Throwable cause) {
        super(cause);
    }
    public ApiNetworkException() {
        super();
    }}
