package com.toy.common.exception;

/** 인증 실패 (토큰 없음, 비밀번호 오류) → 401 Unauthorized */
public class AuthorizationException extends BaseException {

    public AuthorizationException(String message) {
        super(message, 401);
    }
}
