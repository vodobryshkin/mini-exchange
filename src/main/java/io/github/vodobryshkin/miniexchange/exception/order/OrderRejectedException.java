package io.github.vodobryshkin.miniexchange.exception.order;

/**
 * Исключение, возникающее при нарушении правил размещения или изменения заявки.
 *
 * @author vodobryshkin
 * @since 19.11.2026 09:28
 */
public class OrderRejectedException extends RuntimeException {
    /**
     * Конструктор исключения.
     *
     * @param message причина отклонения заявки.
     */
    public OrderRejectedException(String message) {
        super(message);
    }
}