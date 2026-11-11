package com.kungo.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String code, Object... args) {
        super(HttpStatus.UNAUTHORIZED, code, args);
    }
}
