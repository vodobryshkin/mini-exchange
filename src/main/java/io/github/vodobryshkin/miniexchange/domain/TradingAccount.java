package io.github.vodobryshkin.miniexchange.domain;

import io.github.vodobryshkin.miniexchange.domain.common.Currency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

/**
 * Класс, представляющий счёт пользователя на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 13:09
 */
@RequiredArgsConstructor
public class TradingAccount {
    @Positive(message = "ID_MUST_BE_POSITIVE")
    private Long id;

    @Positive(message = "USER_ID_MUST_BE_POSITIVE")
    private final long userId;

    @Min(value = 0, message = "BALANCE_MUST_BE_NON_NEGATIVE")
    private BigDecimal balance;

    @NotNull(message = "CURRENCY_CANNOT_BE_NULL")
    private Currency currency;
}
