package dev.ecasept.unitodo.server.api;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

@Serializable
public class ApiResponse<T> {
        @Field(tag=1)
        private final boolean success;
        @Field(tag=2, nullable=true)
        private final String error;
        @Field(tag=3, nullable=true)
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
}
