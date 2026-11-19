package io.github.vodobryshkin.miniexchange.exception.order;

/**
 * Исключение, которое выбрасывается, когда не удалось найти заявку.
 *
 * @author vodobryshkin
 * @since 18.11.2026 23:47
 */
public class OrderNotFoundException extends RuntimeException {
    /**
     * Конструктор исключения.
     *
     * @param message сведения об отсутствующей заявке.
     */
    public OrderNotFoundException(String message) {
        super("ORDER_NOT_FOUND: " + message);
    }
}
