package com.moe.socialnetwork.common.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseAPI<T> {
    private int code;
    private String message;
    private T data;
    private Map<String, String> errors;

    public static <T> ResponseAPI<T> of(int code, String message, T data) {
        return ResponseAPI.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseAPI<T> error(int code, String message, Map<String, String> errors) {
        return ResponseAPI.<T>builder()
                .code(code)
                .message(message)
                .errors(errors)
                .build();
    }
}
