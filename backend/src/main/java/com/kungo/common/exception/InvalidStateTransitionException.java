package com.kungo.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Levée lorsqu'une machine à états métier (ex: {@code BookingStatus}) reçoit
 * une transition non autorisée depuis l'état courant.
 */
public class InvalidStateTransitionException extends BusinessException {
    public InvalidStateTransitionException(String code, Object... args) {
        super(HttpStatus.CONFLICT, code, args);
    }
}
