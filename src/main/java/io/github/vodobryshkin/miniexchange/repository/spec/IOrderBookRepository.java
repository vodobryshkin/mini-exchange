package io.github.vodobryshkin.miniexchange.repository.spec;

import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.orderbook.response.OrderBookLevelResponse;
import java.time.Instant;
import java.util.List;

/**
 * Интерфейс репозитория биржевого стакана, определяющий методы
 * для получения агрегированных ценовых уровней.
 *
 * @author vodobryshkin
 * @since 19.11.2026 07:10
 */
public interface IOrderBookRepository {
    /**
     * Метод для получения лучших ценовых уровней одной стороны биржевого стакана.
     *
     * @param secCode код торгового инструмента.
     * @param direction направление заявок.
     * @param depth максимальное количество ценовых уровней.
     * @param asOf момент формирования снимка стакана.
     * @return агрегированные ценовые уровни в порядке от лучшей цены.
     */
    List<OrderBookLevelResponse> findLevels(
            SecCode secCode, OrderDirection direction, int depth, Instant asOf
    );
}