package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.orderbook.response.OrderBookLevelResponse;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderBookRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Реализация репозитория биржевого стакана, использующая чистый JDBC для взаимодействия с базой данных.
 *
 * @author vodobryshkin
 * @since 19.11.2026 07:04
 */
@Repository
@RequiredArgsConstructor
public class JdbcOrderBookRepository implements IOrderBookRepository {
    private static final RowMapper<OrderBookLevelResponse> LEVEL_ROW_MAPPER =
            (rs, rowNum) -> new OrderBookLevelResponse(
                    rs.getBigDecimal("price"),
                    rs.getBigDecimal("quantity").toBigIntegerExact(),
                    rs.getLong("order_count")
            );

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод для получения лучших ценовых уровней одной стороны биржевого стакана.
     *
     * @param secCode код торгового инструмента.
     * @param direction направление заявок.
     * @param depth максимальное количество ценовых уровней.
     * @return агрегированные ценовые уровни в порядке от лучшей цены.
     */
    @Override
    public List<OrderBookLevelResponse> findLevels(
            SecCode secCode,
            OrderDirection direction,
            int depth) {

        String orderIdColumn = switch (direction) {
            case BUY -> "buy_order_id";
            case SELL -> "sell_order_id";
        };

        String sortDirection = switch (direction) {
            case BUY -> "DESC";
            case SELL -> "ASC";
        };

        String sql = """
                SELECT o.price,
                       SUM(o.quantity - COALESCE(f.executed_quantity, 0)) AS quantity,
                       COUNT(*) AS order_count
                FROM orders o
                LEFT JOIN LATERAL (
                    SELECT SUM(t.quantity) AS executed_quantity
                    FROM trades t
                    WHERE t.%s = o.order_id
                ) f ON TRUE
                WHERE o.sec_code = ?::SEC_CODE
                  AND o.direction = ?::ORDER_DIRECTION
                  AND o.status = 'NEW'::STATUS
                  AND o.type = 'LIMIT'::ORDER_TYPE
                  AND o.tif = 'DAY'::ORDER_TIF
                  AND o.creation_time >= (
                      date_trunc('day', CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow')
                      AT TIME ZONE 'Europe/Moscow'
                  )
                  AND o.creation_time <= CURRENT_TIMESTAMP
                  AND o.quantity - COALESCE(f.executed_quantity, 0) > 0
                GROUP BY o.price
                ORDER BY o.price %s
                LIMIT ?
                """.formatted(orderIdColumn, sortDirection);

        return jdbcTemplate.query(
                sql,
                LEVEL_ROW_MAPPER,
                secCode.getValue(),
                direction.name(),
                depth
        );
    }
}