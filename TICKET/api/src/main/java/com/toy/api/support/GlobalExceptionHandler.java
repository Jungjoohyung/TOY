package com.toy.api.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    // @Valid 검증 실패 시 (400 Bad Request)
    // "이메일 형식이 아닙니다", "비밀번호는 8자 이상" 같은 메시지를 JSON으로 예쁘게 줍니다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("🚨 [입력값 검증 실패] 잘못된 데이터가 들어왔습니다.");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // 1. [기존] 비즈니스 에러 (예: 이미 예약된 좌석) -> 409 Conflict
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.warn("⚠️ [예약 거절] {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 👇 [여기 추가함!] 2. 인증 실패 및 잘못된 요청 (예: 토큰 없음, 비번 틀림) -> 401 Unauthorized
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        
        // 이것도 시스템 에러가 아니므로 WARN 레벨
        log.warn("🚨 [요청 거절] {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // 401 반환
                .body(new ErrorResponse(e.getMessage()));
    }

    // 3. [기존] 예측 못한 시스템 에러 (NPE 등) -> 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("🚨 [긴급 장애] 예측 못한 에러 발생!", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버에 문제가 생겼습니다. 관리자에게 문의하세요."));
    }

    // 에러 응답용 DTO
    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}