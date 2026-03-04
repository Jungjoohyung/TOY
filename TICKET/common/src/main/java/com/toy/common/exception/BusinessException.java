package com.toy.common.exception;

/** 비즈니스 규칙 위반 (이미 예약됨, 중복 가입 등) → 409 Conflict */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, 409);
    }
}
