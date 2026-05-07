package com.Connectedm.backend.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;

    // 1. 성공 응답(데이터가 있을 때)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data , "요청이 성공적으로 처리되었습니다.");
    }
    // 2. 성공 응답(데이터가 없을 때, 단순 완료 메시지용)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
    // 3. 실패 응답(GlobalExceptionHandler에서 사용)
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
