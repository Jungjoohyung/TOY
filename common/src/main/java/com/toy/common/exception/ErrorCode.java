package com.toy.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // Common
    INTERNAL_SERVER_ERROR(500, "C001", "내부 서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(400, "C002", "잘못된 입력 값입니다."),
    METHOD_NOT_ALLOWED(405, "C003", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(404, "C004", "엔티티를 찾을 수 없습니다."),
    
    // Business
    BUSINESS_ERROR(400, "B001", "비즈니스 로직 오류가 발생했습니다.");
    
    private final int status;
    private final String code;
    private final String message;
}
