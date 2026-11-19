\set ON_ERROR_STOP on
\ir variables.sql
BEGIN;
SELECT id AS market_lock FROM exchange_state WHERE id = 1 FOR UPDATE \gset
DELETE FROM trades
WHERE buy_order_id IN (
    SELECT order_id FROM orders
    WHERE sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code')
) OR sell_order_id IN (
    SELECT order_id FROM orders
    WHERE sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code')
);
DELETE FROM orders
WHERE sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code');
DELETE FROM positions
WHERE trading_account_id IN (
    SELECT a.trading_account_id FROM trading_accounts a JOIN users u USING (user_id)
    WHERE u.username IN (:'buyer_username', :'seller_username')
);
DELETE FROM trading_accounts
WHERE user_id IN (
    SELECT user_id FROM users WHERE username IN (:'buyer_username', :'seller_username')
);
DELETE FROM spring_session
WHERE principal_name IN (:'buyer_username', :'seller_username');
DELETE FROM users WHERE username IN (:'buyer_username', :'seller_username');
DELETE FROM trading_instruments
WHERE sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code');
COMMIT;
