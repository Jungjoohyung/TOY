package com.toy.api.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 👈 모든 컨트롤러의 에러를 여기서 잡겠다!
public class GlobalExceptionHandler {

    // 우리가 Seat.java에서 던진 IllegalStateException을 여기서 딱 잡습니다.
    // 1. [비즈니스 에러] 우리가 예상한 에러 (예: 이미 예약된 좌석)
    // 이건 "시스템 장애"가 아니라 "유저의 요청 거절"이므로 WARN 레벨로 찍습니다.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        
        // 로그 남기기: "누가 이미 예약된 걸 찔렀네? 정도만 알면 됨"
        log.warn("⚠️ [예약 거절] {}", e.getMessage());
        // 500(서버 에러) 대신 409(Conflict: 충돌)를 리턴합니다.
        // 메시지는 우리가 적은 "이미 예약된 좌석입니다"가 나갑니다.
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 2. [시스템 에러] 우리가 예측 못한 진짜 버그 (NPE, DB 다운 등)
    // 이건 진짜 500 에러니까 ERROR 레벨로 찍고, 개발자한테 알림 가야 함!
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        
        // 📝 로그 남기기: 에러 위치(Stack Trace)까지 다 찍어야 고침!
        log.error("🚨 [긴급 장애] 예측 못한 에러 발생!", e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버에 문제가 생겼습니다. 관리자에게 문의하세요."));
    }

    // 에러 응답용 DTO (Inner Class로 간단히 작성)
    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}