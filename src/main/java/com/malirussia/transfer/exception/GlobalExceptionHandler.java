package com.malirussia.transfer.exception;

import com.malirussia.transfer.exception.ApiException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Centralise la traduction des erreurs métier. Spring lit automatiquement
 * l'en-tête Accept-Language envoyé par le frontend (voir api.js côté React)
 * pour choisir entre messages.properties / messages_ru.properties /
 * messages_en.properties.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> gererApiException(ApiException e) {
        String message = messageSource.getMessage(e.getCleMessage(), null, e.getCleMessage(), LocaleContextHolder.getLocale());
        return ResponseEntity.status(e.getStatus()).body(Map.of("detail", message));
    }
}
