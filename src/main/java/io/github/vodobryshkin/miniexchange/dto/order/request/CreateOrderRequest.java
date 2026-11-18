package io.github.vodobryshkin.miniexchange.dto.order.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

/**
 * DTO для запроса на создание заявки на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:17
 */
@Data
public class CreateOrderRequest {
    @JsonProperty("sec_code")
    @Valid
    @NotNull(message = "SEC_CODE_CANNOT_BE_NULL")
    private SecCode secCode;

    @JsonProperty("trading_account_id")
    @Positive(message = "TRADING_ACCOUNT_ID_MUST_BE_POSITIVE")
    private long tradingAccountId;

    @NotNull(message = "DIRECTION_CANNOT_BE_NULL")
    private OrderDirection direction;

    //TODO убрать тип заявки из ДТО на создание заявки
    @NotNull(message = "ORDER_TYPE_CANNOT_BE_NULL")
    private OrderType type;

    @JsonProperty("time_in_force")
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
