package io.github.vodobryshkin.miniexchange.dto.order;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для проверки средств сторон перед заключением сделки.
 *
 * @author vodobryshkin
 * @since 19.11.2026 09:01
 */
@Data
@AllArgsConstructor
public class TradeResources {
    private final boolean sameOwner;
    private final BigDecimal buyerBalance;
    private final long sellerQuantity;
}