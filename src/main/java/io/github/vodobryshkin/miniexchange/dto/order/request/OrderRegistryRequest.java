package io.github.vodobryshkin.miniexchange.dto.order.request;

import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для запроса на получение реестра заявок.
 *
 * @author vodobryshkin
 * @since 19.11.2026 00:36
 */
@Data
@AllArgsConstructor
public class OrderRegistryRequest {
    @Valid
    @NotNull(message = "REGISTRY_PARAMETERS_CANNOT_BE_NULL")
    private RegistryRequest registry;

    private OrderStatus status;

    private OrderDirection direction;
}