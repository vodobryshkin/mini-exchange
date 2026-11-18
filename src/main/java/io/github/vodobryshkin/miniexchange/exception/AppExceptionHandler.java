package io.github.vodobryshkin.miniexchange.exception;

import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * @return HTTP-ответ с сообщением в зависимости от типа исключения.
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
     * @return HTTP-ответ с сообщением в зависимости от типа исключения.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<AuthResponse> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new AuthResponse(e.getMessage()));
    }
}
