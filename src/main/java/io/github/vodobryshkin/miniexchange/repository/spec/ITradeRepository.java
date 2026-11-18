package io.github.vodobryshkin.miniexchange.repository.spec;

import io.github.vodobryshkin.miniexchange.domain.Trade;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import java.util.List;

/**
 * Интерфейс репозитория заключенных сделок.
 *
 * @author vodobryshkin
 * @since 19.11.2026 01:35
 */
public interface ITradeRepository {
    /**
     * Метод для получения страницы сделок с участием счетов пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return сделки в порядке убывания времени сделки и идентификатора.
     */
    List<Trade> findTrades(long userId, RegistryRequest request);

    /**
     * Метод для подсчета сделок с учетом фильтров и прав доступа.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации.
     * @return общее количество подходящих сделок.
     */
    long countTrades(long userId, RegistryRequest request);
}
