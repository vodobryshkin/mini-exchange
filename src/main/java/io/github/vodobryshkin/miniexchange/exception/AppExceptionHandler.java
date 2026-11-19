package io.github.vodobryshkin.miniexchange.exception;

import io.github.vodobryshkin.miniexchange.dto.auth.response.AuthResponse;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderOperationResponse;
import io.github.vodobryshkin.miniexchange.dto.orderbook.request.OrderBookRequest;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotActiveException;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotCancelledException;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.order.OrderRejectedException;
import io.github.vodobryshkin.miniexchange.exception.tradingaccount.TradingAccountNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.tradinginstrument.TradingInstrumentNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.user.UserNotFoundException;
import io.github.vodobryshkin.miniexchange.exception.user.UserWithThisUsernameAlreadyExistsException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
     * @param ex исключение с информацией об отсутствующем пользователе.
     * @return HTTP-ответ с описанием ошибки и статусом NOT FOUND.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<AuthResponse> handleUserNotFoundException(
            UserNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new AuthResponse(ex.getMessage()));
    }

    /**
     * Метод для обработки исключения TradingAccountNotFoundException.
     *
     * @param ex исключение с информацией об отсутствующем торговом счёте.
     * @return HTTP-ответ с описанием ошибки и статусом NOT FOUND.
     */
    @ExceptionHandler(TradingAccountNotFoundException.class)
    public ResponseEntity<TradingAccountNotFoundException> handleTradingAccountNotFoundException(
            TradingAccountNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new TradingAccountNotFoundException(ex.getMessage()));
    }

    /**
     * Метод для обработки исключения TradingInstrumentNotFoundException.
     *
     * @param ex исключение с информацией об отсутствующем торговом инструменте.
     * @return HTTP-ответ с описанием ошибки и статусом NOT FOUND.
     */
    @ExceptionHandler(TradingInstrumentNotFoundException.class)
    public ResponseEntity<TradingInstrumentNotFoundException> handleTradingInstrumentNotFoundException(
            TradingInstrumentNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new TradingInstrumentNotFoundException(ex.getMessage()));
    }

    /**
     * Метод для обработки исключения OrderNotFoundException.
     *
     * @param ex исключение с информацией об отсутствующей заявке.
     * @return HTTP-ответ с описанием ошибки и статусом NOT FOUND.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<OrderNotFoundException> handleOrderNotFoundException(
            OrderNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new OrderNotFoundException(ex.getMessage()));
    }

    /**
     * Метод для обработки исключения OrderNotCancelledException.
     *
     * @param ex исключение с информацией об ошибке отмены заявки.
     * @return HTTP-ответ с описанием ошибки и статусом CONFLICT.
     */
    @ExceptionHandler(OrderNotCancelledException.class)
    public ResponseEntity<OrderNotCancelledException> handleOrderNotCancelledException(
            OrderNotCancelledException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new OrderNotCancelledException(ex.getMessage()));
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

    /**
     * Метод для обработки ошибок валидации и привязки параметров запроса.
     *
     * @param ex исключение с информацией об ошибках.
     * @return HTTP-ответ с описанием ошибок и соответствующим статусом.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Map<String, List<String>>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Object target = ex.getBindingResult().getTarget();
        boolean orderBookRequest = target instanceof OrderBookRequest;

        Map<String, List<String>> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .collect(Collectors.groupingBy(
                                FieldError::getField,
                                Collectors.mapping(
                                        error -> orderBookRequest && error.isBindingFailure()
                                                ? "DEPTH_MUST_BE_AN_INTEGER_BETWEEN_1_AND_100"
                                                : error.getDefaultMessage(),
                                        Collectors.toList()
                                )
                        ));

        HttpStatus status = target instanceof RegistryRequest || orderBookRequest
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.UNPROCESSABLE_ENTITY;

        return ResponseEntity
                .status(status)
                .body(Map.of("errors", errors));
    }

    /**
     * Метод для обработки ошибок проверки вручную собранных параметров запроса.
     *
     * @param ex исключение с нарушениями ограничений.
     * @return HTTP-ответ с описанием ошибок и статусом BAD REQUEST.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Map<String, List<String>>>> handleConstraintViolationException(
            ConstraintViolationException ex) {

        Map<String, List<String>> errors =
                ex.getConstraintViolations().stream()
                        .collect(Collectors.groupingBy(
                                violation -> violation.getPropertyPath().toString(),
                                Collectors.mapping(
                                        ConstraintViolation::getMessage,
                                        Collectors.toList()
                                )
                        ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", errors));
    }

    /**
     * Метод для обработки нарушения правил размещения заявки.
     *
     * @param ex исключение с кодом нарушения.
     * @return HTTP-ответ с описанием ошибки и статусом CONFLICT.
     */
    @ExceptionHandler(OrderRejectedException.class)
    public ResponseEntity<OrderOperationResponse> handleOrderRejectedException(
            OrderRejectedException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new OrderOperationResponse(ex.getMessage()));
    }

    /**
     * Метод для обработки попытки изменения неактивной заявки.
     *
     * @return HTTP-ответ с описанием ошибки и статусом CONFLICT.
     */
    @ExceptionHandler(OrderNotActiveException.class)
    public ResponseEntity<OrderOperationResponse> handleOrderNotActiveException() {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new OrderOperationResponse("ORDER_NOT_ACTIVE"));
    }
}