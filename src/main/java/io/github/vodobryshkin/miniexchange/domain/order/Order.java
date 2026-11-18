package io.github.vodobryshkin.miniexchange.domain.order;

import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

/**
 * Класс, представляющий заявку на покупку или продажу торговых инструментов на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 13:46
 */
@RequiredArgsConstructor
public class Order {
    @Positive(message = "ID_MUST_BE_POSITIVE")
    private Long id;

    @Valid
    @NotNull(message = "SEC_CODE_CANNOT_BE_NULL")
    private final SecCode secCode;

    @Positive(message = "TRADING_ACCOUNT_ID_MUST_BE_POSITIVE")
    private final long tradingAccountId;

    @NotNull(message = "DIRECTION_CANNOT_BE_NULL")
    private final OrderDirection direction;

    @NotNull(message = "ORDER_TYPE_CANNOT_BE_NULL")
    private final OrderType type;

    @NotNull(message = "TIME_IN_FORCE_CANNOT_BE_NULL")
    private TimeInForce timeInForce;

    @NotNull(message = "PRICE_CANNOT_BE_NULL")
    @Positive(message = "PRICE_MUST_BE_POSITIVE")
    private BigDecimal price;

    @Positive(message = "QUANTITY_MUST_BE_POSITIVE")
    private long quantity;

    @NotNull(message = "STATUS_CANNOT_BE_NULL")
    private OrderStatus status;
}
