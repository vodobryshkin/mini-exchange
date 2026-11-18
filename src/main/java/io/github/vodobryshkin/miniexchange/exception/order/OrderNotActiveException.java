package io.github.vodobryshkin.miniexchange.exception.order;

/**
 * Исключение, которое выбрасывается, когда удалось найти заявку, но она неактивна.
 *
 * @author vodobryshkin
 * @since 18.11.2026 23:47
 */
public class OrderNotActiveException extends RuntimeException {
    public OrderNotActiveException(String message) {
        super("ORDER_NOT_ACTIVE: " + message);
    }
}