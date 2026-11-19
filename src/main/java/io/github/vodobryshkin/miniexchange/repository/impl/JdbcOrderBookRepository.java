package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.orderbook.response.OrderBookLevelResponse;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderBookRepository;
import java.sql.Timestamp;
import java.time.Instant;
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
     * @param asOf момент формирования снимка стакана.
     * @return агрегированные ценовые уровни в порядке от лучшей цены.
     */
    @Override
    public List<OrderBookLevelResponse> findLevels(
            SecCode secCode,
            OrderDirection direction,
            int depth,
            Instant asOf) {

        String sortDirection = switch (direction) {
            case BUY -> "DESC";
            case SELL -> "ASC";
        };

        String sql = """
                SELECT price,
                       SUM(quantity - filled_quantity) AS quantity,
                       COUNT(*) AS order_count
                FROM orders
                WHERE sec_code = ?::SEC_CODE
                  AND direction = ?::ORDER_DIRECTION
                  AND status = 'NEW'
                  AND type = 'LIMIT'
                  AND tif = 'DAY'
                  AND NOT matching_pending
                  AND quantity > filled_quantity
                  AND expires_at > ?
                GROUP BY price
                ORDER BY price %s
                LIMIT ?
                """.formatted(sortDirection);

        return jdbcTemplate.query(
                sql,
                LEVEL_ROW_MAPPER,
                secCode.getValue(),
                direction.name(),
                Timestamp.from(asOf),
                depth
        );
    }
}