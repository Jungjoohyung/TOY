package com.toy.common.exception;

/** 권한 없음 (타인의 리소스 접근) → 403 Forbidden */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message, 403);
    }
}
