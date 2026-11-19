--liquibase formatted sql

--changeset vodobryshkin:015-matching-state dbms:postgresql splitStatements:true

ALTER TYPE status RENAME VALUE 'CANCELED' TO 'CANCELLED';

ALTER TABLE trading_accounts
    ALTER COLUMN balance TYPE NUMERIC(38, 8);

CREATE TABLE exchange_state (
                                id INTEGER PRIMARY KEY CHECK (id = 1)
);

INSERT INTO exchange_state(id) VALUES (1);

CREATE SEQUENCE order_queue_sequence;

ALTER TABLE orders
    ADD COLUMN initial_quantity BIGINT,
    ADD COLUMN filled_quantity BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN expires_at TIMESTAMPTZ,
    ADD COLUMN queue_sequence BIGINT,
    ADD COLUMN matching_pending BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE orders
SET initial_quantity = quantity;

UPDATE orders o
SET filled_quantity = f.quantity
FROM (
         SELECT order_id, SUM(quantity) AS quantity
         FROM (
                  SELECT buy_order_id AS order_id, quantity
                  FROM trades

                  UNION ALL

                  SELECT sell_order_id AS order_id, quantity
                  FROM trades
              ) sides
         GROUP BY order_id
     ) f
WHERE f.order_id = o.order_id;

UPDATE orders
SET expires_at = (
                     date_trunc(
                             'day',
                             creation_time AT TIME ZONE 'Europe/Moscow'
                     ) + INTERVAL '1 day'
                     ) AT TIME ZONE 'Europe/Moscow';

WITH priorities AS (
    SELECT order_id,
           ROW_NUMBER() OVER (
               ORDER BY creation_time, order_id
               ) AS value
    FROM orders
)
UPDATE orders o
SET queue_sequence = p.value
FROM priorities p
WHERE p.order_id = o.order_id;

SELECT setval(
               'order_queue_sequence',
               COALESCE(MAX(queue_sequence), 0) + 1,
               FALSE
       )
FROM orders;

UPDATE orders
SET status = 'FILLED'
WHERE status = 'NEW'
  AND quantity = filled_quantity;

UPDATE orders
SET matching_pending = (status = 'NEW');

ALTER TABLE orders
    ALTER COLUMN initial_quantity SET NOT NULL,
    ALTER COLUMN expires_at SET NOT NULL,
    ALTER COLUMN queue_sequence SET NOT NULL,

    ADD CONSTRAINT orders_initial_quantity_positive
        CHECK (initial_quantity > 0),

    ADD CONSTRAINT orders_filled_range
        CHECK (filled_quantity BETWEEN 0 AND quantity),

    ADD CONSTRAINT orders_filled_status
        CHECK (
            status <> 'FILLED'
                OR filled_quantity = quantity
            ),

    ADD CONSTRAINT orders_active_remainder
        CHECK (
            status <> 'NEW'
                OR filled_quantity < quantity
            ),

    ADD CONSTRAINT orders_queue_sequence_unique
        UNIQUE (queue_sequence);

ALTER TABLE trades
    ADD CONSTRAINT trades_positive_price
        CHECK (price > 0),

    ADD CONSTRAINT trades_positive_quantity
        CHECK (quantity > 0),

    ADD CONSTRAINT trades_distinct_orders
        CHECK (buy_order_id <> sell_order_id);

CREATE INDEX idx_orders_matching_pending
    ON orders(queue_sequence)
    WHERE status = 'NEW'
        AND matching_pending;

CREATE INDEX idx_orders_expiration
    ON orders(expires_at, order_id)
    WHERE status = 'NEW';

CREATE INDEX idx_orders_matching_buy
    ON orders(sec_code, price DESC, queue_sequence)
    WHERE status = 'NEW'
        AND direction = 'BUY'
        AND NOT matching_pending;

CREATE INDEX idx_orders_matching_sell
    ON orders(sec_code, price, queue_sequence)
    WHERE status = 'NEW'
        AND direction = 'SELL'
        AND NOT matching_pending;