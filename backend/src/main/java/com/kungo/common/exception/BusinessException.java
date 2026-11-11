package com.kungo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Racine de la hiérarchie des exceptions métier applicatives.
 * Chaque exception porte un code i18n (résolu via MessageSource) plutôt
 * qu'un message figé, afin de supporter FR/EN.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final transient Object[] args;

    public BusinessException(HttpStatus status, String code, Object... args) {
        super(code);
        this.status = status;
        this.code = code;
        this.args = args;
    }
}
