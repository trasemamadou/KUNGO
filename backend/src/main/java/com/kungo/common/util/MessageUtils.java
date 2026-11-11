package com.kungo.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Résout un code i18n en message localisé (FR/EN selon {@code Accept-Language}) pour
 * les messages de confirmation renvoyés dans {@code ApiResponse} (le
 * {@code GlobalExceptionHandler} gère déjà la résolution côté erreurs).
 */
@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    public String get(String code, Object... args) {
        return messageSource.getMessage(code, args, code, LocaleContextHolder.getLocale());
    }
}
