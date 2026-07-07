package dev.ecasept.unitodo.client.api.exception;

import dev.ecasept.unitodo.shared.models.api.ErrorCode;

public class ApiServerErrorException extends ApiException {
    private final ErrorCode errorCode;

    public ApiServerErrorException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    public ApiServerErrorException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    public ApiServerErrorException(Throwable cause, ErrorCode errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }
    public ApiServerErrorException(ErrorCode errorCode) {
        super();
        this.errorCode = errorCode;
    }
    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
