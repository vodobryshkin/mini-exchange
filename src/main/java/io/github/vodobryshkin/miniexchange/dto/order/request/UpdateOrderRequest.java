package io.github.vodobryshkin.miniexchange.dto.order.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

/**
 * DTO для запроса на обновление заявки на бирже.
 *
 * @author vodobryshkin
 * @since 19.11.2026 00:16
 */
@Data
public class UpdateOrderRequest {
    @JsonProperty("time_in_force")
    @NotNull(message = "TIME_IN_FORCE_CANNOT_BE_NULL")
    private TimeInForce timeInForce;

    /**
     * Новая цена заявки.
     *
     * @mock 101.00
     */
    @NotNull(message = "PRICE_CANNOT_BE_NULL")
    @Positive(message = "PRICE_MUST_BE_POSITIVE")
    private BigDecimal price;

    /**
     * Новое общее количество инструмента в штуках.
     *
     * @mock 12
     */
    @Positive(message = "QUANTITY_MUST_BE_POSITIVE")
    private long quantity;
}
