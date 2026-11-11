package com.kungo.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String code, Object... args) {
        super(HttpStatus.NOT_FOUND, code, args);
    }

    public static ResourceNotFoundException of(String entityMessageCode, Object identifier) {
        return new ResourceNotFoundException(entityMessageCode, identifier);
    }
}
