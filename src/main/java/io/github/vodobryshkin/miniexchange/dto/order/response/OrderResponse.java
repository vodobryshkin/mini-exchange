package io.github.vodobryshkin.miniexchange.dto.order.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * DTO для представления данных о заявке с биржи.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:27
 */
@Data
@Builder
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponse {
    private final Long id;
    private final String secCode;
    private final long tradingAccountId;
    private final OrderDirection direction;
    private final OrderType type;
    private final TimeInForce timeInForce;
    private final BigDecimal price;
    private final long initialQuantity;
    private final long quantity;
    private final long filledQuantity;
    private final long remainingQuantity;
    private final OrderStatus status;
    private final Instant creationTime;
    private final Instant expiresAt;
    private final long queueSequence;
    private final boolean matchingPending;
}