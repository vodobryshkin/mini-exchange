package io.github.vodobryshkin.miniexchange.repository.impl;

import io.github.vodobryshkin.miniexchange.domain.order.Order;
import io.github.vodobryshkin.miniexchange.domain.order.OrderDirection;
import io.github.vodobryshkin.miniexchange.domain.order.OrderStatus;
import io.github.vodobryshkin.miniexchange.domain.order.OrderType;
import io.github.vodobryshkin.miniexchange.domain.order.TimeInForce;
import io.github.vodobryshkin.miniexchange.domain.tradinginstrument.SecCode;
import io.github.vodobryshkin.miniexchange.dto.common.request.RegistryRequest;
import io.github.vodobryshkin.miniexchange.dto.order.TradeResources;
import io.github.vodobryshkin.miniexchange.dto.order.request.OrderRegistryRequest;
import io.github.vodobryshkin.miniexchange.repository.spec.IOrderRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
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
    private static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, rowNum) -> Order.builder()
            .id(rs.getLong("order_id"))
            .secCode(new SecCode(rs.getString("sec_code")))
            .tradingAccountId(rs.getLong("trading_account_id"))
            .direction(OrderDirection.valueOf(rs.getString("direction")))
            .type(OrderType.valueOf(rs.getString("type")))
            .timeInForce(TimeInForce.valueOf(rs.getString("tif")))
            .price(rs.getBigDecimal("price"))
            .initialQuantity(rs.getLong("initial_quantity"))
            .quantity(rs.getLong("quantity"))
            .filledQuantity(rs.getLong("filled_quantity"))
            .status(OrderStatus.valueOf(rs.getString("status")))
            .creationTime(rs.getTimestamp("creation_time").toInstant())
            .expiresAt(rs.getTimestamp("expires_at").toInstant())
            .queueSequence(rs.getLong("queue_sequence"))
            .matchingPending(rs.getBoolean("matching_pending"))
            .build();

    private final JdbcTemplate jdbcTemplate;

    /**
     * Метод для сохранения заявки в репозитории.
     *
     * @param order заявку, которую необходимо сохранить.
     */
    @Override
    public void save(Order order) {
        if (order.getId() == null) {
            String sql = """
                    INSERT INTO orders(
                        sec_code, trading_account_id, direction, type, tif,
                        price, initial_quantity, quantity, filled_quantity, status,
                        creation_time, expires_at, queue_sequence, matching_pending
                    )
                    VALUES (
                        ?::SEC_CODE, ?, ?::ORDER_DIRECTION, ?::ORDER_TYPE, ?::ORDER_TIF,
                        ?, ?, ?, ?, ?::STATUS, ?, ?, ?, ?
                    )
                    RETURNING order_id
                    """;

            Long id = jdbcTemplate.queryForObject(
                    sql,
                    Long.class,
                    order.getSecCode().getValue(),
                    order.getTradingAccountId(),
                    order.getDirection().name(),
                    order.getType().name(),
                    order.getTimeInForce().name(),
                    order.getPrice(),
                    order.getInitialQuantity(),
                    order.getQuantity(),
                    order.getFilledQuantity(),
                    order.getStatus().name(),
                    Timestamp.from(order.getCreationTime()),
                    Timestamp.from(order.getExpiresAt()),
                    order.getQueueSequence(),
                    order.isMatchingPending()
            );

            order.setId(id);
            return;
        }

        String sql = """
                UPDATE orders
                SET price = ?,
                    quantity = ?,
                    filled_quantity = ?,
                    status = ?::STATUS,
                    queue_sequence = ?,
                    matching_pending = ?,
                    update_time = clock_timestamp()
                WHERE order_id = ?
                """;

        requireOne(jdbcTemplate.update(
                sql,
                order.getPrice(),
                order.getQuantity(),
                order.getFilledQuantity(),
                order.getStatus().name(),
                order.getQueueSequence(),
                order.isMatchingPending(),
                order.getId()
        ));
    }

    /**
     * Метод для получения заявки по её идентификатору.
     *
     * @param id идентификатор заявки.
     * @return заявку с указанным идентификатором.
     */
    @Override
    public Optional<Order> findOrderById(long id) {
        return jdbcTemplate.query(
                "SELECT * FROM orders WHERE order_id = ?",
                ORDER_ROW_MAPPER,
                id
        ).stream().findFirst();
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

        String sql = "SELECT o.* "
                + buildWhereClause(userId, request, parameters)
                + """
                ORDER BY o.creation_time DESC, o.order_id DESC
                LIMIT ? OFFSET ?
                """;

        parameters.add(registry.getSize());
        parameters.add(registry.getOffset());

        return jdbcTemplate.query(
                sql,
                ORDER_ROW_MAPPER,
                parameters.toArray()
        );
    }

    /**
     * Метод для подсчёта заявок с учётом фильтров и прав доступа.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации.
     * @return общее количество подходящих заявок.
     */
    @Override
    public long countOrders(long userId, OrderRegistryRequest request) {
        List<Object> parameters = new ArrayList<>();

        String sql = "SELECT COUNT(*) "
                + buildWhereClause(userId, request, parameters);

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                parameters.toArray()
        );
    }

    /**
     * Метод для формирования условий выборки заявок.
     *
     * @param userId идентификатор пользователя.
     * @param request параметры фильтрации.
     * @param parameters список параметров SQL-запроса.
     * @return SQL-фрагмент с таблицами и условиями выборки.
     */
    private String buildWhereClause(
            long userId,
            OrderRegistryRequest request,
            List<Object> parameters) {

        RegistryRequest registry = request.getRegistry();

        StringBuilder sql = new StringBuilder("""
                FROM orders o
                JOIN trading_accounts a
                    ON a.trading_account_id = o.trading_account_id
                WHERE a.user_id = ?
                """);

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

    /**
     * Метод для блокировки торговых изменений до завершения транзакции.
     */
    @Override
    public void lockMarket() {
        jdbcTemplate.queryForObject(
                "SELECT id FROM exchange_state WHERE id = 1 FOR UPDATE",
                Integer.class
        );
    }

    /**
     * Метод для получения следующего номера очереди.
     *
     * @return номер постановки заявки в очередь.
     */
    @Override
    public long nextSequence() {
        return jdbcTemplate.queryForObject(
                "SELECT nextval('order_queue_sequence')",
                Long.class
        );
    }

    /**
     * Метод для проверки валюты, шага цены и кратности количества.
     *
     * @param order проверяемая заявка.
     * @return true, если параметры соответствуют правилам инструмента.
     */
    @Override
    public boolean validTerms(Order order) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM trading_instruments i
                    JOIN trading_accounts a ON a.trading_account_id = ?
                    WHERE i.sec_code = ?::SEC_CODE
                      AND a.currency = i.quote_currency
                      AND ? > 0
                      AND ? < 10000000000
                      AND ? = trunc(?::numeric, 8)
                      AND mod(?::numeric, i.min_step) = 0
                      AND ? % i.lot_size = 0
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                sql,
                Boolean.class,
                order.getTradingAccountId(),
                order.getSecCode().getValue(),
                order.getPrice(),
                order.getPrice(),
                order.getPrice(),
                order.getPrice(),
                order.getPrice(),
                order.getQuantity()
        ));
    }

    /**
     * Метод для получения первой ожидающей обработки заявки.
     *
     * @return заблокированную заявку, если очередь не пуста.
     */
    @Override
    public Optional<Order> findPending() {
        String sql = """
                SELECT *
                FROM orders
                WHERE status = 'NEW'
                  AND matching_pending
                ORDER BY queue_sequence
                LIMIT 1
                FOR UPDATE
                """;

        return jdbcTemplate.query(sql, ORDER_ROW_MAPPER)
                .stream()
                .findFirst();
    }

    /**
     * Метод для получения наиболее приоритетной встречной заявки.
     *
     * @param incoming входящая заявка.
     * @param now момент проверки срока действия.
     * @return заблокированную встречную заявку, если она существует.
     */
    @Override
    public Optional<Order> findCounterparty(Order incoming, Instant now) {
        boolean buy = incoming.getDirection() == OrderDirection.BUY;

        String sql = """
                SELECT *
                FROM orders
                WHERE sec_code = ?::SEC_CODE
                  AND direction = ?::ORDER_DIRECTION
                  AND status = 'NEW'
                  AND type = 'LIMIT'
                  AND tif = 'DAY'
                  AND NOT matching_pending
                  AND quantity > filled_quantity
                  AND expires_at > ?
                  AND price %s ?
                  AND order_id <> ?
                ORDER BY price %s, queue_sequence
                LIMIT 1
                FOR UPDATE
                """.formatted(
                buy ? "<=" : ">=",
                buy ? "ASC" : "DESC"
        );

        return jdbcTemplate.query(
                sql,
                ORDER_ROW_MAPPER,
                incoming.getSecCode().getValue(),
                buy ? "SELL" : "BUY",
                Timestamp.from(now),
                incoming.getPrice(),
                incoming.getId()
        ).stream().findFirst();
    }

    /**
     * Метод для получения первой просроченной активной заявки.
     *
     * @param now момент проверки срока действия.
     * @return заблокированную заявку, если она существует.
     */
    @Override
    public Optional<Order> findExpired(Instant now) {
        String sql = """
                SELECT *
                FROM orders
                WHERE status = 'NEW'
                  AND expires_at <= ?
                ORDER BY expires_at, order_id
                LIMIT 1
                FOR UPDATE
                """;

        return jdbcTemplate.query(
                sql,
                ORDER_ROW_MAPPER,
                Timestamp.from(now)
        ).stream().findFirst();
    }

    /**
     * Метод для блокировки счетов и получения средств сторон сделки.
     *
     * @param buy заявка покупки.
     * @param sell заявка продажи.
     * @return средства и сведения о владельцах сторон.
     */
    @Override
    public TradeResources lockResources(Order buy, Order sell) {
        jdbcTemplate.queryForList(
                """
                SELECT trading_account_id
                FROM trading_accounts
                WHERE trading_account_id IN (?, ?)
                ORDER BY trading_account_id
                FOR UPDATE
                """,
                Long.class,
                buy.getTradingAccountId(),
                sell.getTradingAccountId()
        );

        String sql = """
                SELECT b.user_id = s.user_id AS same_owner,
                       b.balance AS buyer_balance,
                       COALESCE(p.quantity, 0) AS seller_quantity
                FROM trading_accounts b
                JOIN trading_accounts s ON s.trading_account_id = ?
                LEFT JOIN positions p
                    ON p.trading_account_id = s.trading_account_id
                   AND p.sec_code = ?::SEC_CODE
                WHERE b.trading_account_id = ?
                """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new TradeResources(
                        rs.getBoolean("same_owner"),
                        rs.getBigDecimal("buyer_balance"),
                        rs.getLong("seller_quantity")
                ),
                sell.getTradingAccountId(),
                sell.getSecCode().getValue(),
                buy.getTradingAccountId()
        );
    }

    /**
     * Метод для сохранения сделки и изменения денег и позиций.
     *
     * @param buy заявка покупки.
     * @param sell заявка продажи.
     * @param price цена исполнения.
     * @param quantity количество исполнения.
     * @param now момент исполнения.
     */
    @Override
    public void settle(
            Order buy,
            Order sell,
            BigDecimal price,
            long quantity,
            Instant now) {

        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity));

        requireOne(jdbcTemplate.update(
                """
                UPDATE trading_accounts
                SET balance = balance - ?
                WHERE trading_account_id = ?
                  AND balance >= ?
                """,
                cost,
                buy.getTradingAccountId(),
                cost
        ));

        requireOne(jdbcTemplate.update(
                """
                UPDATE trading_accounts
                SET balance = balance + ?
                WHERE trading_account_id = ?
                """,
                cost,
                sell.getTradingAccountId()
        ));

        requireOne(jdbcTemplate.update(
                """
                UPDATE positions
                SET quantity = quantity - ?
                WHERE trading_account_id = ?
                  AND sec_code = ?::SEC_CODE
                  AND quantity >= ?
                """,
                quantity,
                sell.getTradingAccountId(),
                sell.getSecCode().getValue(),
                quantity
        ));

        requireOne(jdbcTemplate.update(
                """
                INSERT INTO positions(trading_account_id, sec_code, quantity)
                VALUES (?, ?::SEC_CODE, ?)
                ON CONFLICT (trading_account_id, sec_code)
                DO UPDATE
                SET quantity = positions.quantity + EXCLUDED.quantity
                """,
                buy.getTradingAccountId(),
                buy.getSecCode().getValue(),
                quantity
        ));

        requireOne(jdbcTemplate.update(
                """
                INSERT INTO trades(
                    buy_order_id, sell_order_id, price, quantity, trade_time
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                buy.getId(),
                sell.getId(),
                price,
                quantity,
                Timestamp.from(now)
        ));
    }

    /**
     * Метод для проверки количества изменённых строк.
     *
     * @param updated количество изменённых строк.
     */
    private void requireOne(int updated) {
        if (updated != 1) {
            throw new IllegalStateException("INCONSISTENT_TRADING_STATE");
        }
    }
}