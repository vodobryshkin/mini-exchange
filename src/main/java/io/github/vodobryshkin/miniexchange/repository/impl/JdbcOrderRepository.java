package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderRepository;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
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

    /**
     * Метод для сохранения заявки в репозитории.
     *
     * @param order заявку, которую необходимо сохранить.
     */
    @Override
    public void save(Order order) {
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
}