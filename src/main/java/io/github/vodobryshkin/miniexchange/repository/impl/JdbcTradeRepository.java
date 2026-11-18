package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.Trade;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.repository.spec.ITradeRepository;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Реализация репозитория сделок, использующая JDBC для взаимодействия с базой данных.
 *
 * @author vodobryshkin
 * @since 19.11.2026 01:37
 */
@Repository
@RequiredArgsConstructor
public class JdbcTradeRepository implements ITradeRepository {
    private static final RowMapper<Trade> TRADE_ROW_MAPPER = (rs, rowNum) ->
            new Trade(
                    rs.getLong("trade_id"),
                    rs.getLong("buy_order_id"),
                    rs.getLong("sell_order_id"),
                    rs.getBigDecimal("price"),
                    rs.getLong("quantity"),
                    new SecCode(rs.getString("sec_code")),
                    rs.getLong("buy_trading_account_id"),
                    rs.getLong("sell_trading_account_id"),
                    rs.getTimestamp("trade_time").toInstant()
            );

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод для получения страницы сделок с участием счетов пользователя.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return сделки в порядке убывания времени сделки и идентификатора.
     */
    @Override
    public List<Trade> findTrades(long userId, RegistryRequest request) {
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(userId, request, parameters);

        String selectSql = """
                SELECT t.trade_id, t.buy_order_id, t.sell_order_id, t.price, t.quantity,
                       t.trade_time, b.sec_code,
                       b.trading_account_id AS buy_trading_account_id,
                       s.trading_account_id AS sell_trading_account_id
                """;

        String paginationSql = """
                ORDER BY t.trade_time DESC, t.trade_id DESC
                LIMIT ? OFFSET ?
                """;

        String sql = selectSql + whereClause + paginationSql;

        parameters.add(request.getSize());
        parameters.add(request.getOffset());

        return jdbcTemplate.query(sql, TRADE_ROW_MAPPER, parameters.toArray());
    }

    /**
     * Метод для подсчета сделок с учетом фильтров и прав доступа.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации.
     * @return общее количество подходящих сделок.
     */
    @Override
    public long countTrades(long userId, RegistryRequest request) {
        List<Object> parameters = new ArrayList<>();
        String sql = "SELECT COUNT(*) " + buildWhereClause(userId, request, parameters);

        return jdbcTemplate.queryForObject(sql, Long.class, parameters.toArray());
    }

    /**
     * Метод для формирования условий выборки сделок.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации.
     * @param parameters список параметров SQL-запроса.
     * @return SQL-фрагмент с таблицами и условиями выборки.
     */
    private String buildWhereClause(
            long userId,
            RegistryRequest request,
            List<Object> parameters) {

        String baseSql = """
                FROM trades t
                JOIN orders b ON b.order_id = t.buy_order_id
                JOIN orders s ON s.order_id = t.sell_order_id
                JOIN trading_accounts ba ON ba.trading_account_id = b.trading_account_id
                JOIN trading_accounts sa ON sa.trading_account_id = s.trading_account_id
                WHERE (
                """;

        StringBuilder sql = new StringBuilder(baseSql);

        sql.append(" (ba.user_id = ? ");
        parameters.add(userId);

        if (request.getTradingAccountId() != null) {
            sql.append(" AND b.trading_account_id = ? ");
            parameters.add(request.getTradingAccountId());
        }

        sql.append(") OR (sa.user_id = ? ");
        parameters.add(userId);

        if (request.getTradingAccountId() != null) {
            sql.append(" AND s.trading_account_id = ? ");
            parameters.add(request.getTradingAccountId());
        }

        sql.append(")) ");

        if (request.getSecCode() != null) {
            sql.append(" AND b.sec_code = ?::SEC_CODE ");
            parameters.add(request.getSecCode());
        }

        if (request.getFrom() != null) {
            sql.append(" AND t.trade_time >= ? ");
            parameters.add(Timestamp.from(request.getFrom().toInstant()));
        }

        if (request.getTo() != null) {
            sql.append(" AND t.trade_time < ? ");
            parameters.add(Timestamp.from(request.getTo().toInstant()));
        }

        return sql.toString();
    }
}