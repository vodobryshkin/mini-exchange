package io.github.vodobryshkin.miniexchange.dto.tradingaccount.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа на запросы, связанные с операциями над торговым счётом.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:28
 */
@Data
@AllArgsConstructor
public class TradingAccountOperationResponse {
    private final String message;
}