package io.github.vodobryshkin.miniexchange.dto.orderbook.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Параметры получения биржевого стакана.
 *
 * @author vodobryshkin
 * @since 19.11.2026 06:25
 */
@Getter
@Setter
public class OrderBookRequest {
    @Min(value = 1, message = "DEPTH_MUST_BE_BETWEEN_1_AND_100")
    @Max(value = 100, message = "DEPTH_MUST_BE_BETWEEN_1_AND_100")
    private int depth = 20;
}
