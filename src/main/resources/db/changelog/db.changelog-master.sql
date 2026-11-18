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