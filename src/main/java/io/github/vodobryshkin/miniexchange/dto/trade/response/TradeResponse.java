package io.github.vodobryshkin.miniexchange.dto.trade.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для представления заключенной сделки в реестре.
 *
 * @author vodobryshkin
 * @since 19.11.2026 01:20
 */
@Data
@AllArgsConstructor
public class TradeResponse {
    private Long id;

    @JsonProperty("sec_code")
    private String secCode;

    @JsonProperty("buy_order_id")
    private long buyOrderId;

    @JsonProperty("sell_order_id")
    private long sellOrderId;

    @JsonProperty("buy_trading_account_id")
    private long buyTradingAccountId;

    @JsonProperty("sell_trading_account_id")
    private long sellTradingAccountId;

    private BigDecimal price;

    private long quantity;

    @JsonProperty("trade_time")
    private Instant tradeTime;
}