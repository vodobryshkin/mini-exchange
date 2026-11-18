package io.github.vodobryshkin.miniexchange.domain;

import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 *  Класс, представляющий конкретную позицию, которая лежит на счёте.
 *
 * @author vodobryshkin
 * @since 18.11.2026 13:06
 */
@RequiredArgsConstructor
public class Position {
    @Positive(message = "ID_MUST_BE_POSITIVE")
    private Long id;

    @Positive(message = "TRADING_ACCOUNT_ID_MUST_BE_POSITIVE")
    private final long tradingAccountId;

    @Valid
    @NotNull(message = "SEC_CODE_CANNOT_BE_NULL")
    private final SecCode secCode;

    @Min(value = 0, message = "QUANTITY_MUST_BE_POSITIVE")
    private long quantity;
}
