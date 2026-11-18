package io.github.vodobryshkin.miniexchange.domain.tradinginstrument;

import io.github.vodobryshkin.miniexchange.domain.common.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Класс, представляющий торговый инструмент на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 14:07
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class TradingInstrument {
    @Valid
    @NotNull(message = "SEC_CODE_CANNOT_BE_NULL")
    private final SecCode secCode;

    @NotNull(message = "TRADING_INSTRUMENT_TYPE_CANNOT_BE_NULL")
    private final TradingInstrumentType type;

    @Positive(message = "LOT_SIZE_MUST_BE_POSITIVE")
    private int lotSize;

    @NotNull(message = "MIN_STEP_CANNOT_BE_NULL")
    @Positive(message = "MIN_STEP_MUST_BE_POSITIVE")
    private BigDecimal minStep;

    @NotNull(message = "QUOTE_CURRENCY_CANNOT_BE_NULL")
    private final Currency quoteCurrency;
}
