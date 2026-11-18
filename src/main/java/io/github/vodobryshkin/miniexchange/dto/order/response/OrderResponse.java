package io.github.vodobryshkin.miniexchange.dto.order.response;

import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import java.math.BigDecimal;
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
    private String secCode;
    private long tradingAccountId;
    private OrderDirection direction;
    private OrderType type;
    private TimeInForce timeInForce;
    private BigDecimal price;
    private long quantity;
    private OrderStatus status;
}
