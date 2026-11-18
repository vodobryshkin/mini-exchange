package io.github.vodobryshkin.miniexchange.mapper;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.dto.order.request.CreateOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Маппер заявок.
 *
 * @author vodobryshkin
 * @since 18.11.2026 22:43
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    Order toOrder(CreateOrderRequest request);
}