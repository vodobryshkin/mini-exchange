package io.github.vodobryshkin.miniexchange.domain.tradinginstrument;

import io.github.vodobryshkin.miniexchange.domain.common.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

/**
 * Класс, представляющий торговый инструмент на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 14:07
 */
@RequiredArgsConstructor
public class TradingInstrument {
    @Valid
    private final SecCode secCode;

    @NotNull(message = "TRADING_INSTRUMENT_TYPE_CANNOT_BE_NULL")
    private final TradingInstrumentType type;

    @Positive(message = "LOT_SIZE_MUST_BE_POSITIVE")
    private int lotSize;

    @Positive(message = "MIN_STEP_MUST_BE_POSITIVE")
    private BigDecimal minStep;

    @NotNull(message = "QUOTE_CURRENCY_CANNOT_BE_NULL")
    private final Currency quoteCurrency;
}
