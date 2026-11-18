package io.github.vodobryshkin.miniexchange.dto.tradinginstument.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа на запросы, связанные с операциями с торговым инструментом.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:28
 */
@Data
@AllArgsConstructor
public class TradingInstrumentOperationResponse {
    private final String message;
}
