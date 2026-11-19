package io.github.vodobryshkin.miniexchange.dto.orderbook.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO для представления ценового уровня биржевого стакана.
 *
 * @author vodobryshkin
 * @since 19.11.2026 07:00
 */
@Getter
@AllArgsConstructor
public class OrderBookLevelResponse {
    private final BigDecimal price;

    private final BigInteger quantity;

    @JsonProperty("order_count")
    private final long orderCount;
}