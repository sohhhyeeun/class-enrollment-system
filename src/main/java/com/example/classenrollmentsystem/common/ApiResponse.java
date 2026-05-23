package com.example.classenrollmentsystem.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private final String code;
    private final String message;
    private final T data;

    // 데이터 포함
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    // 데이터 미포함
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", null);
    }
}
