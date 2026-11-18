package io.github.vodobryshkin.miniexchange.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

/**
 * Класс, представляющий сделку на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 14:25
 */
@RequiredArgsConstructor
public class Execution {
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
}
