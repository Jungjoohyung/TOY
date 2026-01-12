package com.toy.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    
    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;
}
