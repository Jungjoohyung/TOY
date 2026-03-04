package com.toy.common.exception;

public abstract class BaseException extends RuntimeException {

    private final int httpStatus;

    protected BaseException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
