package com.kungo.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    public ForbiddenException(String code, Object... args) {
        super(HttpStatus.FORBIDDEN, code, args);
    }
}
