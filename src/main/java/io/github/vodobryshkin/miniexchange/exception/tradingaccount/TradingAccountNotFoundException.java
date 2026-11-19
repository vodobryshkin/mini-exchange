package io.github.vodobryshkin.miniexchange.exception.tradingaccount;

/**
 * Исключение, которое выбрасывается, когда не удалось найти заявку с указанным идентификатором.
 *
 * @author vodobryshkin
 * @since 18.11.2026 22:15
 */
public class TradingAccountNotFoundException extends RuntimeException {
    /**
     * Конструктор исключения.
     *
     * @param message сообщение об отсутствующем торговом счёте.
     */
    public TradingAccountNotFoundException(String message) {
        super(message);
    }
}
