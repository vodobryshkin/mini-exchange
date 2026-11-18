package io.github.vodobryshkin.miniexchange.domain;

import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Класс, представляющий сделку на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 14:25
 */
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class Trade {
    @Positive(message = "ID_MUST_BE_POSITIVE")
    private Long id;

    @Positive(message = "BUY_ORDER_ID_MUST_BE_POSITIVE")
    private final long buyOrderId;

    @Positive(message = "SELL_ORDER_ID_MUST_BE_POSITIVE")
    private final long sellOrderId;

    @NotNull(message = "PRICE_CANNOT_BE_NULL")
    @Positive(message = "PRICE_MUST_BE_POSITIVE")
    private final BigDecimal price;

    @Positive(message = "QUANTITY_MUST_BE_POSITIVE")
    private final long quantity;

    @Valid
    @NotNull(message = "SEC_CODE_CANNOT_BE_NULL")
    private final SecCode secCode;

    @Positive(message = "BUY_TRADING_ACCOUNT_ID_MUST_BE_POSITIVE")
    private final long buyTradingAccountId;

    @Positive(message = "SELL_TRADING_ACCOUNT_ID_MUST_BE_POSITIVE")
    private final long sellTradingAccountId;

    @NotNull(message = "TRADE_TIME_CANNOT_BE_NULL")
    private final Instant tradeTime;
}