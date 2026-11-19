package io.github.vodobryshkin.miniexchange.dto.orderbook.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO для представления биржевого стакана и его показателей.
 *
 * @author vodobryshkin
 * @since 19.11.2026 6:40
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class OrderBookResponse {
    @JsonProperty("sec_code")
    private final String secCode;

    private final int depth;

    private final List<OrderBookLevelResponse> bids;

    private final List<OrderBookLevelResponse> asks;

    @JsonProperty("best_bid")
    private final BigDecimal bestBid;

    @JsonProperty("best_ask")
    private final BigDecimal bestAsk;

    private final BigDecimal spread;

    @JsonProperty("market_depth")
    private final MarketDepthResponse marketDepth;
}