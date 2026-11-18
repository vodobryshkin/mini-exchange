package io.github.vodobryshkin.miniexchange.dto.order.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа на запросы, связанные с операциями над заявками на бирже.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:28
 */
@Data
@AllArgsConstructor
public class OrderOperationResponse {
    private final String message;

    public static final OrderOperationResponse CREATED = new OrderOperationResponse("CREATED");
}
