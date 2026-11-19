package io.github.vodobryshkin.miniexchange.mapper;

import io.github.vodobryshkin.miniexchange.domain.Trade;
import io.github.vodobryshkin.miniexchange.dto.trade.response.TradeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Маппер заключенных сделок.
 *
 * @author vodobryshkin
 * @since 19.11.2026 01:27
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TradeMapper {
    /**
     * Метод для преобразования сделки в запись реестра.
     *
     * @param trade заключенная сделка.
     * @return DTO с информацией о сделке.
     */
    @Mapping(target = "secCode", source = "secCode.value")
    TradeResponse toTradeResponse(Trade trade);
}