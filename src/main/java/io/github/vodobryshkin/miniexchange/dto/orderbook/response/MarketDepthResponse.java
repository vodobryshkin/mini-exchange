package io.github.vodobryshkin.miniexchange.dto.orderbook.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO для представления суммарного доступного количества заявок
 * на покупку и продажу в пределах возвращённых уровней стакана.
 *
 * @author vodobryshkin
 * @since 19.11.2026 08:00
 */
@Getter
@AllArgsConstructor
public class MarketDepthResponse {
    @JsonProperty("bid_quantity")
    private final BigInteger bidQuantity;

    @JsonProperty("ask_quantity")
    private final BigInteger askQuantity;
}