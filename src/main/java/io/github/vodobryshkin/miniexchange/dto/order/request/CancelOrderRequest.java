package io.github.vodobryshkin.miniexchange.dto.order.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO для запроса на удаление заявки на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 23:08
 */
@Data
public class CancelOrderRequest {
    @Positive(message = "ID_MUST_BE_POSITIVE")
    private long orderId;
}
