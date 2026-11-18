package io.github.vodobryshkin.miniexchange.dto.order.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для представления данных о заявке с биржи.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:27
 */
@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;

    @JsonProperty("sec_code")
    private String secCode;

    @JsonProperty("trading_account_id")
    private long tradingAccountId;

    private OrderDirection direction;
    private OrderType type;

    @JsonProperty("time_in_force")
    private TimeInForce timeInForce;

    private BigDecimal price;
    private long quantity;
    private OrderStatus status;

    @JsonProperty("creation_time")
    private Instant creationTime;
}