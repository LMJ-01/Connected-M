package com.Connectedm.backend.global.error;

import com.Connectedm.backend.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // ✨ 모든 컨트롤러의 에러를 여기서 가로채서 포장합니다.
public class GlobalExceptionHandler {

    // 1. 명준님이 만든 CustomException 처리 (이메일 중복, 닉네임 중복 등)
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException e) {
        // 400 Bad Request 상태코드와 함께 실제 에러 메시지를 보냅니다.
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    // 2. DB 에러 처리 (전화번호가 너무 길어서 생기는 에러 등)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataException(Exception e) {
        return ResponseEntity.badRequest().body(ApiResponse.error("입력값이 너무 길거나 올바르지 않습니다. DB 설정을 확인하세요!"));
    }

    // 3. 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleAllException(Exception e) {
        return ResponseEntity.internalServerError().body(ApiResponse.error("서버 내부 오류가 발생했습니다."));
    }
}