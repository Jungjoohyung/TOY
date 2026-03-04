package com.toy.common.exception;

/** 존재하지 않는 리소스에 접근 시 → 404 Not Found */
public class EntityNotFoundException extends BaseException {

    public EntityNotFoundException(String message) {
        super(message, 404);
    }
}
