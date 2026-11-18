package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Реализация репозитория заявок, использующая чистый JDBC для взаимодействия с базой данных.
 *
 * @author vodobryshkin
 * @since 18.11.2026 21:39
 */
@Repository
@RequiredArgsConstructor
public class JdbcOrderRepository implements IOrderRepository {
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
}