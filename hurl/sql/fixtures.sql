\set ON_ERROR_STOP on
BEGIN;
SELECT id AS market_lock FROM exchange_state WHERE id = 1 FOR UPDATE \gset
SELECT user_id AS buyer_id FROM users WHERE username = :'buyer_username' \gset
SELECT user_id AS seller_id FROM users WHERE username = :'seller_username' \gset

INSERT INTO trading_instruments(sec_code, type, lot_size, min_step, quote_currency)
VALUES (:'sec_code', 'STOCK', :lot_size, 0.01, 'RUB'),
       (:'second_code', 'STOCK', 1, 0.01, 'RUB'),
       (:'empty_code', 'STOCK', 1, 0.01, 'RUB'),
       (:'day_code', 'STOCK', 1, 0.01, 'RUB'),
       (:'reverse_code', 'STOCK', 1, 0.01, 'RUB');

INSERT INTO trading_accounts(user_id, balance, currency)
VALUES (:buyer_id, 10000, 'RUB')
RETURNING trading_account_id AS buyer_account \gset
INSERT INTO trading_accounts(user_id, balance, currency)
VALUES (:buyer_id, 0, 'RUB')
RETURNING trading_account_id AS buyer_second_account \gset
INSERT INTO trading_accounts(user_id, balance, currency)
VALUES (:seller_id, 10000, 'RUB')
RETURNING trading_account_id AS seller_account \gset

INSERT INTO positions(trading_account_id, sec_code, quantity)
SELECT :buyer_account, sec_code, 0 FROM trading_instruments
WHERE sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code');
INSERT INTO positions(trading_account_id, sec_code, quantity)
SELECT :buyer_second_account, sec_code, 100 FROM trading_instruments
WHERE sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code');
INSERT INTO positions(trading_account_id, sec_code, quantity)
SELECT :seller_account, sec_code,
       CASE WHEN :case_no = 3 AND sec_code = :'empty_code' THEN 0 ELSE 100 END
FROM trading_instruments
WHERE sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code');

INSERT INTO orders(sec_code, trading_account_id, direction, type, tif,
    price, quantity, initial_quantity, filled_quantity, status,
    creation_time, expires_at, queue_sequence, matching_pending)
SELECT :'day_code', :buyer_account, 'BUY', 'LIMIT', 'DAY', 10, 1, 1, 0, 'NEW',
       clock_timestamp() - INTERVAL '1 day', clock_timestamp() - INTERVAL '1 second',
       nextval('order_queue_sequence'), FALSE
WHERE :case_no = 3;
COMMIT;

\echo buyer_id=:buyer_id
\echo seller_id=:seller_id
\echo buyer_account=:buyer_account
\echo buyer_second_account=:buyer_second_account
\echo seller_account=:seller_account
