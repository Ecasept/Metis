package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.utils.ThrowingBiFunction;
import dev.ecasept.unitodo.shared.utils.ThrowingFunction;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Represents the response of the API, which can either be a success with data or an error with an error message an associated info. */
public class ApiResponse<T> {
        private final boolean success;
        private final String error;
        private final ErrorCode errorCode;
        private final T data;

        private ApiResponse(boolean success, String error, ErrorCode errorCode, T data) {
            this.success = success;
            this.error = error;
            this.data = data;
            this.errorCode = errorCode;
        }

        /** Creates a successful ApiResponse with the given data. */
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, null, null, data);
        }

        /** Creates an error ApiResponse with the given error message and code. */
        public static <T> ApiResponse<T> error(String errorMessage, ErrorCode errorCode) {
            return new ApiResponse<>(false, errorMessage, errorCode, null);
        }

    /** Executes specified functions based on the content of the ApiResponse.
     *
     * @param onSuccess Executed if the ApiResponse is successful, and receives the data
     * @param onError Executed if the ApiResponse is an error, and receives the error message
     * @return The result of the executed function
     * @param <R> The return type of the executed function
     * @param <E> The type of exception that can be thrown by the executed function
     * @throws E If the executed function throws an exception
     */
        public <R, E extends Throwable> R on(ThrowingFunction<T, R, E> onSuccess, ThrowingBiFunction<String, ErrorCode, R, E> onError) throws E {
            if (success) {
                return onSuccess.apply(data);
            } else {
                return onError.apply(error, errorCode);
            }
        }
        public void on(Consumer<T> onSuccess, BiConsumer<String, ErrorCode> onError) {
            if (success) {
                onSuccess.accept(data);
            } else {
                onError.accept(error, errorCode);
            }
        }
}
