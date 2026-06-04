package dev.ecasept.unitodo.shared.models;

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

        public ApiResponse<T> on(Consumer<T> onSuccess, Consumer<String> onError) {
            if (success) {
                onSuccess.accept(data);
            } else {
                onError.accept(error);
            }
            return this;
        }
        public ApiResponse<T> onSuccess(Consumer<T> onSuccess) {
            if (success) {
                onSuccess.accept(data);
            }
            return this;
        }
        public ApiResponse<T> onError(Consumer<String> onError) {
            if (!success) {
                onError.accept(error);
            }
            return this;
        }
}
