CREATE TABLE transactions (
    id varchar(36) primary key,
    idempotency_key varchar(128) not null unique,
    account_id varchar(64) not null,
    counterparty_account varchar(64) not null,
    amount decimal(15, 2) not null,
    currency varchar(3) not null,
    status varchar(32) not null,
    created_at TIMESTAMP default current_timestamp,
    updated_at TIMESTAMP default current_timestamp on update current_timestamp,
    version BIGINT not null default 0,
    INDEX idx_transactions_account (account_id),
    INDEX idx_transactions_status (status)
    )ENGINE = InnoDB default CHARSET=utf8mb4;

CREATE TABLE accounts(
    id varchar(64) primary key,
    owner_name varchar(128) not null,
    balance decimal(15,5) not null default 0.00,
    currency varchar(3) not null,
    version BIGINT not null default 0,
    updated_at TIMESTAMP default current_timestamp on update current_timestamp
)ENGINE = InnoDB default CHARSET=utf8mb4;

-- Seed default test accounts
INSERT INTO accounts (id, owner_name, balance, currency, version)
VALUES
    ('ACC-1001', 'Alice Johnson', 50000.00, 'GBP', 0),
    ('ACC-1002', 'Barclays Treasury Clearing', 1000000.00, 'GBP', 0);