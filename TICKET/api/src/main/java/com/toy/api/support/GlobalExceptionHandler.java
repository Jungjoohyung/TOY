package com.toy.api.support;

import com.toy.common.exception.BaseException;
import com.toy.common.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외 (BaseException 하위 전부):
     * EntityNotFoundException(404), AuthorizationException(401),
     * ForbiddenException(403), BusinessException(409)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        log.warn("[{}] {}", e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.error(e.getHttpStatus(), e.getMessage()));
    }

    /**
     * @Valid 검증 실패 → 400 Bad Request
     * 필드별 에러 메시지를 콤마로 합쳐서 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("[ValidationError] {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity
                .status(400)
                .body(ApiResponse.error(400, message));
    }

    /**
     * 예상치 못한 서버 에러 → 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[ServerError] 예측 못한 에러 발생!", e);
        return ResponseEntity
                .status(500)
                .body(ApiResponse.error(500, "서버에 문제가 생겼습니다. 관리자에게 문의하세요."));
    }
}
