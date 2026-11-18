package io.github.vodobryshkin.miniexchange.service.spec;

import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.common.response.PageResponse;
import io.github.vodobryshkin.miniexchange.dto.trade.response.TradeResponse;

/**
 * Сервис для получения данных о заключенных сделках.
 *
 * @author vodobryshkin
 * @since 19.11.2026 02:01
 */
public interface ITradeService {
    /**
     * Метод для получения реестра сделок пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return страница реестра сделок.
     */
    PageResponse<TradeResponse> getTrades(long userId, RegistryRequest request);
}
