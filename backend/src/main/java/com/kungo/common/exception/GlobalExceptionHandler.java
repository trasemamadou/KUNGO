package com.kungo.common.exception;

import com.kungo.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Gestionnaire global d'exceptions : traduit toute exception en {@link ErrorResponse}
 * cohérente, localisée (FR/EN via {@code Accept-Language}), sans jamais exposer de
 * stacktrace ni de détail d'implémentation au client.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        String message = resolveMessage(ex.getCode(), ex.getArgs(), request.getLocale());
        log.warn("BusinessException [{}]: {}", ex.getCode(), message);
        return build(ex.getStatus(), message, path(request), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        String message = resolveMessage("error.validation", null, request.getLocale());
        return build(HttpStatus.BAD_REQUEST, message, path(request), fieldErrors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        String message = resolveMessage("error.forbidden", null, request.getLocale());
        return build(HttpStatus.FORBIDDEN, message, path(request), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        String message = resolveMessage("user.invalid-credentials", null, request.getLocale());
        return build(HttpStatus.UNAUTHORIZED, message, path(request), null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, WebRequest request) {
        String message = resolveMessage("user.account-not-active", null, request.getLocale());
        return build(HttpStatus.FORBIDDEN, message, path(request), null);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, WebRequest request) {
        String message = resolveMessage("user.account-suspended", null, request.getLocale());
        return build(HttpStatus.FORBIDDEN, message, path(request), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        log.error("Data integrity violation", ex);
        String message = resolveMessage("error.conflict", null, request.getLocale());
        return build(HttpStatus.CONFLICT, message, path(request), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        String message = resolveMessage("error.internal", null, request.getLocale());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, path(request), null);
    }

    private ErrorResponse.FieldError toFieldError(FieldError fe) {
        return ErrorResponse.FieldError.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .rejectedValue(fe.getRejectedValue())
                .build();
    }

    private String resolveMessage(String code, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    private String path(WebRequest request) {
        if (request instanceof org.springframework.web.context.request.ServletWebRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getRequest();
            return httpRequest.getRequestURI();
        }
        return request.getDescription(false);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path,
                                                 List<ErrorResponse.FieldError> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
