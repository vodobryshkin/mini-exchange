package io.github.vodobryshkin.miniexchange.exception.order;

/**
 * Исключение, которое выбрасывается, когда не удалось отменить заявку.
 *
 * @author vodobryshkin
 * @since 18.11.2026 23:38
 */
public class OrderNotCancelledException extends RuntimeException {
    /**
     * Конструктор исключения.
     *
     * @param message причина, по которой заявка не отменена.
     */
    public OrderNotCancelledException(String message) {
        super(message);
    }
}
