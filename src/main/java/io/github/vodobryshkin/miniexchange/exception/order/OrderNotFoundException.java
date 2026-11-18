package io.github.vodobryshkin.miniexchange.exception.order;

/**
 * Исключение, которое выбрасывается, когда не удалось найти заявку.
 *
 * @author vodobryshkin
 * @since 18.11.2026 23:47
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
