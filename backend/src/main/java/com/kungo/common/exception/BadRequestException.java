package com.kungo.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    public BadRequestException(String code, Object... args) {
        super(HttpStatus.BAD_REQUEST, code, args);
    }
}
