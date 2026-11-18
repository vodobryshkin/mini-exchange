--liquibase formatted sql

--changeset vodobryshkin:001-create-enums dbms:postgresql

CREATE TYPE currency AS ENUM (
    'RUB',
    'USD',
    'EUR',
    'CNY',
    'JPY',
    'GBP'
);

CREATE TYPE trading_instrument_type AS ENUM (
    'STOCK',
    'BOND',
    'CURRENCY_PAIR'
);

CREATE TYPE order_direction AS ENUM ('BUY', 'SELL');
CREATE TYPE order_type AS ENUM ('LIMIT');
CREATE TYPE order_tif AS ENUM ('DAY');
CREATE TYPE status AS ENUM ('NEW', 'FILLED', 'CANCELLED', 'EXPIRED');

--rollback DROP TYPE status;
--rollback DROP TYPE order_tif;
--rollback DROP TYPE order_type;
--rollback DROP TYPE order_direction;
--rollback DROP TYPE trading_instrument_type;
--rollback DROP TYPE currency;


--changeset vodobryshkin:002-create-sec-code-domain dbms:postgresql

CREATE DOMAIN sec_code AS VARCHAR(32) CHECK (VALUE ~ '^[A-Z0-9._/-]{1,32}$');

--rollback DROP DOMAIN sec_code;


--changeset vodobryshkin:003-create-users dbms:postgresql

CREATE TABLE users (
                       user_id       BIGSERIAL PRIMARY KEY,
                       username      VARCHAR(64) NOT NULL UNIQUE CHECK (username ~ '^[A-Za-z0-9_]{6,32}$'),
                       password_hash VARCHAR(255) NOT NULL
);

--rollback DROP TABLE users;


--changeset vodobryshkin:004-create-trading-instruments dbms:postgresql

CREATE TABLE trading_instruments (
                                     sec_code       sec_code PRIMARY KEY,
                                     type           trading_instrument_type NOT NULL,
                                     lot_size       INTEGER NOT NULL DEFAULT 1 CHECK (lot_size > 0),
                                     min_step       NUMERIC(18, 8) NOT NULL CHECK (min_step > 0),
                                     quote_currency currency NOT NULL
);

--rollback DROP TABLE trading_instruments;


--changeset vodobryshkin:005-create-trading-accounts dbms:postgresql

CREATE TABLE trading_accounts (
                                  trading_account_id BIGSERIAL PRIMARY KEY,
                                  user_id            BIGINT NOT NULL REFERENCES users,
                                  balance            NUMERIC(18, 8) NOT NULL CHECK (balance >= 0),
                                  currency           currency NOT NULL
);

--rollback DROP TABLE trading_accounts;


--changeset vodobryshkin:006-create-positions dbms:postgresql

CREATE TABLE positions (
                           position_id        BIGSERIAL PRIMARY KEY,
                           trading_account_id BIGINT NOT NULL REFERENCES trading_accounts,
                           sec_code           sec_code NOT NULL REFERENCES trading_instruments,
                           quantity           INTEGER NOT NULL CHECK (quantity >= 0),
                           UNIQUE (trading_account_id, sec_code)
);

--rollback DROP TABLE positions;


--changeset vodobryshkin:007-create-orders dbms:postgresql

CREATE TABLE orders (
                        order_id           BIGSERIAL PRIMARY KEY,
                        sec_code           sec_code NOT NULL REFERENCES trading_instruments,
                        trading_account_id BIGINT NOT NULL REFERENCES trading_accounts,
                        direction          order_direction NOT NULL,
                        type               order_type NOT NULL,
                        tif                order_tif NOT NULL DEFAULT 'DAY',
                        price              NUMERIC(18, 8) NOT NULL CHECK (price > 0),
                        quantity           BIGINT NOT NULL DEFAULT 1 CHECK (quantity > 0),
                        status             status NOT NULL,
                        creation_time      TIMESTAMPTZ NOT NULL DEFAULT now(),
                        update_time        TIMESTAMPTZ NOT NULL DEFAULT now()
);

--rollback DROP TABLE orders;


--changeset vodobryshkin:008-create-executions dbms:postgresql

CREATE TABLE executions (
                            execution_id   BIGSERIAL PRIMARY KEY,
                            buy_order_id   BIGINT NOT NULL REFERENCES orders,
                            sell_order_id  BIGINT NOT NULL REFERENCES orders,
                            price          NUMERIC(18, 8) NOT NULL,
                            quantity       BIGINT NOT NULL,
                            execution_time TIMESTAMPTZ NOT NULL DEFAULT now()
);

--rollback DROP TABLE executions;

--changeset vodobryshkin:009-position-quantity-bigint dbms:postgresql

ALTER TABLE positions
    ALTER COLUMN quantity TYPE BIGINT;

--rollback ALTER TABLE positions ALTER COLUMN quantity TYPE INTEGER;


--changeset vodobryshkin:010-align-canceled-status dbms:postgresql

ALTER TYPE status RENAME VALUE 'CANCELLED' TO 'CANCELED';

--rollback ALTER TYPE status RENAME VALUE 'CANCELED' TO 'CANCELLED';

--changeset vodobryshkin:security-001-create-sessions dbms:postgresql

CREATE TABLE spring_session (
                                primary_id            CHAR(36) PRIMARY KEY,
                                session_id            CHAR(36) NOT NULL UNIQUE,
                                creation_time         BIGINT NOT NULL,
                                last_access_time      BIGINT NOT NULL,
                                max_inactive_interval INTEGER NOT NULL,
                                expiry_time           BIGINT NOT NULL,
                                principal_name        VARCHAR(100)
);

CREATE TABLE spring_session_attributes (
                                           session_primary_id CHAR(36) NOT NULL REFERENCES spring_session ON DELETE CASCADE,
                                           attribute_name     VARCHAR(200) NOT NULL,
                                           attribute_bytes    BYTEA NOT NULL,
                                           PRIMARY KEY (session_primary_id, attribute_name)
);

--rollback DROP TABLE spring_session_attributes;
--rollback DROP TABLE spring_session;

--changeset vodobryshkin:add-query-indexes dbms:postgresql

CREATE INDEX idx_trading_accounts_user_id ON trading_accounts (user_id);

CREATE INDEX idx_positions_sec_code ON positions (sec_code);

CREATE INDEX idx_orders_account_history ON orders (trading_account_id, creation_time DESC, order_id DESC);

CREATE INDEX idx_orders_active_buy ON orders (sec_code, price DESC, creation_time, order_id)
    WHERE status = 'NEW' AND direction = 'BUY';

CREATE INDEX idx_orders_active_sell ON orders (sec_code, price, creation_time, order_id)
    WHERE status = 'NEW' AND direction = 'SELL';

CREATE INDEX idx_orders_active_day_creation_time ON orders (creation_time)
    WHERE status = 'NEW' AND tif = 'DAY';

CREATE INDEX idx_executions_buy_order_id ON executions (buy_order_id);
CREATE INDEX idx_executions_sell_order_id ON executions (sell_order_id);

CREATE INDEX idx_spring_session_expiry_time ON spring_session (expiry_time);
CREATE INDEX idx_spring_session_principal_name ON spring_session (principal_name);

--rollback DROP INDEX idx_spring_session_principal_name;
--rollback DROP INDEX idx_spring_session_expiry_time;
--rollback DROP INDEX idx_executions_sell_order_id;
--rollback DROP INDEX idx_executions_buy_order_id;
--rollback DROP INDEX idx_orders_active_day_creation_time;
--rollback DROP INDEX idx_orders_active_sell;
--rollback DROP INDEX idx_orders_active_buy;
--rollback DROP INDEX idx_orders_account_history;
--rollback DROP INDEX idx_positions_sec_code;
--rollback DROP INDEX idx_trading_accounts_user_id;