package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotActiveException;
import io.github.vodobryshkin.miniexchange.exception.order.OrderNotFoundException;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderRepository;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.validation.annotation.Validated;


/**
 * Реализация репозитория заявок, использующая чистый JDBC для взаимодействия с базой данных.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:39
 */
@Repository
@RequiredArgsConstructor
@Validated
public class JdbcOrderRepository implements IOrderRepository {
    private static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, rowNum) ->
            new Order(
                    rs.getLong("order_id"),
                    new SecCode(rs.getString("sec_code")),
                    rs.getLong("trading_account_id"),
                    OrderDirection.valueOf(rs.getString("direction")),
                    OrderType.valueOf(rs.getString("type")),
                    TimeInForce.valueOf(rs.getString("tif")),
                    rs.getBigDecimal("price"),
                    rs.getLong("quantity"),
                    OrderStatus.valueOf(rs.getString("status"))
            );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Order order) {
        if (order.getId() != null) {
            String selectSql = """
                SELECT status
                FROM orders
                WHERE order_id = ?
                FOR UPDATE
            """;

            OrderStatus currentStatus = jdbcTemplate.query(
                    selectSql,
                    (rs, rowNum) -> OrderStatus.valueOf(rs.getString("status")),
                    order.getId()
            ).stream().findFirst().orElseThrow(() -> new OrderNotFoundException(String.valueOf(order.getId())));

            if (currentStatus != OrderStatus.NEW) {
                throw new OrderNotActiveException(String.valueOf(order.getId()));
            }

            String updateSql = """
            UPDATE orders
            SET tif = ?::ORDER_TIF,
                price = ?,
                quantity = ?,
                status = ?::STATUS,
                update_time = now()
            WHERE order_id = ?
            """;

            jdbcTemplate.update(
                    updateSql,
                    order.getTimeInForce().name(),
                    order.getPrice(),
                    order.getQuantity(),
                    order.getStatus().name(),
                    order.getId()
            );

            return;
        }

        String sql = """
            INSERT INTO orders (
                sec_code, trading_account_id, direction, type,
                tif, price, quantity, status
            )
            VALUES (?::SEC_CODE, ?, ?::ORDER_DIRECTION, ?::ORDER_TYPE, ?::ORDER_TIF, ?, ?, ?::STATUS)
            """;

        jdbcTemplate.update(
                sql,
                order.getSecCode().getValue(),
                order.getTradingAccountId(),
                order.getDirection().name(),
                order.getType().name(),
                order.getTimeInForce().name(),
                order.getPrice(),
                order.getQuantity(),
                order.getStatus().name()
        );
    }

    /**
     * Метод для того, чтобы пометить заявку в репозитории как отмененную.
     *
     * @param orderId id заявки, которую необходимо отменить.
     * @param userId id пользователя, который хочет отменить заявку.
     * @return true, если заявка была успешно отменена, false в противном случае.
     */
    @Override
    public boolean cancel(long orderId, long userId) {
        String sql = """
            UPDATE orders
            SET status = 'CANCELLED'::STATUS, update_time = now()
            WHERE order_id = ? AND status = 'NEW'::STATUS
            """;

        return jdbcTemplate.update(sql, orderId, userId) == 1;
    }

    /**
     * Метод для получения заявки по её идентификатору.
     *
     * @param id идентификатор заявки.
     * @return заявку с указанным идентификатором.
     */
    @Override
    public Optional<Order> findOrderById(long id) {
        String sql = """
            SELECT order_id, sec_code, trading_account_id, direction,
                   type, tif, price, quantity, status
            FROM orders
            WHERE order_id = ?
            """;

        return jdbcTemplate.query(sql, ORDER_ROW_MAPPER, id).stream().findFirst();
    }

    /**
     * Метод для получения страницы заявок по доступным пользователю счетам.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации и пагинации.
     * @return заявки в порядке убывания времени создания и идентификатора.
     */
    @Override
    public List<Order> findOrders(long userId, OrderRegistryRequest request) {
        RegistryRequest registry = request.getRegistry();

        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(userId, request, parameters);

        String selectSql = """
            SELECT o.order_id, o.sec_code, o.trading_account_id, o.direction,
                   o.type, o.tif, o.price, o.quantity, o.status, o.creation_time
            """;

        String paginationSql = """
            ORDER BY o.creation_time DESC, o.order_id DESC
            LIMIT ? OFFSET ?
            """;

        String sql = selectSql + whereClause + paginationSql;

        parameters.add(registry.getSize());
        parameters.add(registry.getOffset());

        return jdbcTemplate.query(sql, ORDER_ROW_MAPPER, parameters.toArray());
    }

    /**
     * Метод для подсчета заявок с учетом фильтров и прав доступа.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации.
     * @return общее количество подходящих заявок.
     */
    @Override
    public long countOrders(long userId, OrderRegistryRequest request) {
        List<Object> parameters = new ArrayList<>();
        String sql = "SELECT COUNT(*) " + buildWhereClause(userId, request, parameters);

        return jdbcTemplate.queryForObject(sql, Long.class, parameters.toArray());
    }

    private String buildWhereClause(
            long userId,
            OrderRegistryRequest request,
            List<Object> parameters) {

        RegistryRequest registry = request.getRegistry();

        String baseSql = """
            FROM orders o
            JOIN trading_accounts a ON a.trading_account_id = o.trading_account_id
            WHERE a.user_id = ?
            """;

        StringBuilder sql = new StringBuilder(baseSql);
        parameters.add(userId);

        if (registry.getTradingAccountId() != null) {
            sql.append(" AND o.trading_account_id = ? ");
            parameters.add(registry.getTradingAccountId());
        }

        if (registry.getSecCode() != null) {
            sql.append(" AND o.sec_code = ?::SEC_CODE ");
            parameters.add(registry.getSecCode());
        }

        if (request.getStatus() != null) {
            sql.append(" AND o.status = ?::STATUS ");
            parameters.add(request.getStatus().name());
        }

        if (request.getDirection() != null) {
            sql.append(" AND o.direction = ?::ORDER_DIRECTION ");
            parameters.add(request.getDirection().name());
        }

        if (registry.getFrom() != null) {
            sql.append(" AND o.creation_time >= ? ");
            parameters.add(Timestamp.from(registry.getFrom().toInstant()));
        }

        if (registry.getTo() != null) {
            sql.append(" AND o.creation_time < ? ");
            parameters.add(Timestamp.from(registry.getTo().toInstant()));
        }

        return sql.toString();
    }
}