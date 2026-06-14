package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.utils.ThrowableFunction;

import java.util.function.Consumer;

public class ApiResponse<T> {
        private final boolean success;
        private final String error;
        private final T data;

        private ApiResponse(boolean success, String error, T data) {
            this.success = success;
            this.error = error;
            this.data = data;
        }

        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, null, data);
        }

        public static <T> ApiResponse<T> error(String errorMessage) {
            return new ApiResponse<>(false, errorMessage, null);
        }

        public <R, E extends Throwable> R on(ThrowableFunction<T, R, E> onSuccess, ThrowableFunction<String, R, E> onError) throws E {
            if (success) {
                return onSuccess.apply(data);
            } else {
                return onError.apply(error);
            }
        }
        public void on(Consumer<T> onSuccess, Consumer<String> onError) {
            if (success) {
                onSuccess.accept(data);
            } else {
                onError.accept(error);
            }
        }
}
