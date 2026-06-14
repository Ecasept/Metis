package dev.ecasept.unitodo.client.api.exception;

public class ApiServerErrorException extends ApiException {
    public ApiServerErrorException(String message) {
        super(message);
    }
    public ApiServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
    public ApiServerErrorException(Throwable cause) {
        super(cause);
    }
    public ApiServerErrorException() {
        super();
    }}
