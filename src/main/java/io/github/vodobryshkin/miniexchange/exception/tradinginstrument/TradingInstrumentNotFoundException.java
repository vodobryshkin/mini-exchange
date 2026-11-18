package io.github.vodobryshkin.miniexchange.exception.tradinginstrument;

/**
 * Исключение, которое выбрасывается, когда не удалось найти торговый инструмент с указанным кодом.
 *
 * @author vodobryshkin
 * @since 18.11.2026 22:35
 */
public class TradingInstrumentNotFoundException extends RuntimeException {
    public TradingInstrumentNotFoundException(String message) {
        super(message);
    }
}
