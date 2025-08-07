package com.example.bankcards.controller.advisor;

import com.example.bankcards.dto.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Централизованный обработчик исключений приложения.
 * Перехватывает и обрабатывает исключения со всех контроллеров.
 */
@Slf4j
@RestControllerAdvice
public class ControllerAdvisor {

    /**
     * Обработка ошибок аутентификации и авторизации.
     */
    @ExceptionHandler({
            AuthenticationException.class,
            AccessDeniedException.class,
            BadCredentialsException.class,
            ExpiredJwtException.class,
            UnsupportedJwtException.class,
            MalformedJwtException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthException(RuntimeException ex) {
        log.warn("Auth error: {}", ex.getMessage());

        HttpStatus status;
        if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else {
            status = HttpStatus.UNAUTHORIZED;
        }

        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getMessage(), "AUTH_ERROR"));
    }

    /**
     * Обработка ошибок валидации данных.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .message("Validation failed")
                        .errorCode("VALIDATION_ERROR")
                        .details(errors)
                        .build());
    }

    /**
     * Обработка ошибок при работе с сущностями (не найдены, конфликты).
     */
    @ExceptionHandler({
            EntityNotFoundException.class,
            NoSuchElementException.class,
            IllegalStateException.class,
            IllegalArgumentException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleBusinessException(RuntimeException ex) {
        log.warn("Business error: {}", ex.getMessage());

        HttpStatus status;
        if (ex instanceof EntityNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status)
                .body(new ErrorResponse(ex.getMessage(), "BUSINESS_ERROR"));
    }

    /**
     * Обработка всех непредвиденных ошибок.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Internal error: ", ex);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
    }
}