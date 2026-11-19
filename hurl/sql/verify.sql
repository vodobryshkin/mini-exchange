\set ON_ERROR_STOP on
\ir variables.sql
SELECT MIN(a.trading_account_id) AS buyer_account
FROM trading_accounts a JOIN users u USING (user_id)
WHERE u.username = :'buyer_username' \gset
SELECT MIN(a.trading_account_id) AS seller_account
FROM trading_accounts a JOIN users u USING (user_id)
WHERE u.username = :'seller_username' \gset
CREATE FUNCTION pg_temp.check_test(ok BOOLEAN, label TEXT) RETURNS VOID
LANGUAGE plpgsql AS $$
BEGIN
    IF ok IS DISTINCT FROM TRUE THEN
        RAISE EXCEPTION 'TEST FAILED: %', label;
    END IF;
END $$;

SELECT pg_temp.check_test(
    NOT EXISTS (
        SELECT 1 FROM trading_accounts a JOIN users u USING (user_id)
        WHERE u.username IN (:'buyer_username', :'seller_username') AND a.balance < 0
    ), 'Balances must remain nonnegative');
SELECT pg_temp.check_test(
    NOT EXISTS (
        SELECT 1 FROM positions p JOIN trading_accounts a USING (trading_account_id)
        JOIN users u USING (user_id)
        WHERE u.username IN (:'buyer_username', :'seller_username') AND p.quantity < 0
    ), 'Positions must remain nonnegative');

SELECT :case_no = 3 AS check_execution \gset
\if :check_execution
SELECT pg_temp.check_test(
    (SELECT balance = 9084 FROM trading_accounts
     WHERE trading_account_id = :buyer_account), 'Buyer balance: 10000 - (3*100 + 2*100 + 1*101) - 3*105');
SELECT pg_temp.check_test(
    (SELECT balance = 10916 FROM trading_accounts
     WHERE trading_account_id = :seller_account), 'Seller balance: 10000 + 601 + 315');
SELECT pg_temp.check_test(
    (SELECT quantity = 6 FROM positions
     WHERE trading_account_id = :buyer_account AND sec_code = :'sec_code'), 'Buyer received 6 units');
SELECT pg_temp.check_test(
    (SELECT quantity = 94 FROM positions
     WHERE trading_account_id = :seller_account AND sec_code = :'sec_code'), 'Seller delivered 6 units');
SELECT pg_temp.check_test(
    (SELECT COUNT(*) = 3 FROM trades t JOIN orders o ON o.order_id = t.buy_order_id
     WHERE o.sec_code = :'sec_code'), 'Exactly three executions, no duplicates');
SELECT pg_temp.check_test(
    NOT EXISTS (
        SELECT 1 FROM orders o
        WHERE o.sec_code IN (:'sec_code', :'second_code', :'empty_code', :'day_code', :'reverse_code')
          AND o.filled_quantity <> COALESCE((
              SELECT SUM(t.quantity) FROM trades t
              WHERE t.buy_order_id = o.order_id OR t.sell_order_id = o.order_id
          ), 0)
    ), 'Order filled quantities agree with trades');
\endif
