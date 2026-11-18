package io.github.vodobryshkin.miniexchange.exception;

import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Класс, который отвечает за перехват и обработку всех выкинутых в программе исключений.
 *
 * @author vodobryshkin
 * @since 18.11.2026 16:28
 */
@RestControllerAdvice
public class AppExceptionHandler {
    /**
     * Метод для обработки исключения UserWithThisUsernameAlreadyExistsException.
     *
     * @return HTTP-ответ с описанием ошибки и статусом CONFLICT.
     */
    @ExceptionHandler(UserWithThisUsernameAlreadyExistsException.class)
    public ResponseEntity<AuthResponse> handleUserWithThisUsernameAlreadyExistsException() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(AuthResponse.USER_ALREADY_EXISTS);
    }

    /**
     * Метод для обработки исключения UserNotFoundException.
     *
     * @return HTTP-ответ с описанием ошибки и статусом NOT FOUND.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<AuthResponse> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new AuthResponse(e.getMessage()));
    }

    /**
     * Метод для обработки исключения AuthenticationException.
     *
     * @return HTTP-ответ с описанием ошибки и статусом UNAUTHORIZED.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthResponse> handleAuthenticationException() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(AuthResponse.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Map<String, List<String>>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        Map<String, Map<String, List<String>>> mapOfErrors = new HashMap<>();
        mapOfErrors.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(mapOfErrors);
    }
}
