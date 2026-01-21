package com.toy.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

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