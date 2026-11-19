package io.github.vodobryshkin.miniexchange.mapper;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.dto.order.response.OrderResponse;
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
    /**
     * Метод для преобразования заявки в DTO ответа.
     *
     * @param order заявку, которую необходимо преобразовать.
     * @return DTO с информацией о заявке.
     */
    @Mapping(target = "secCode", source = "secCode.value")
    OrderResponse toOrderResponse(Order order);
}