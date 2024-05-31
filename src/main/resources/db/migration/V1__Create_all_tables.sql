create
EXTENSION IF NOT EXISTS "uuid-ossp";

create TABLE customers
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(64) NOT NULL,
    last_name  VARCHAR(64) NOT NULL,
    country    VARCHAR(64) NOT NULL
);

create TABLE cards
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    card_number VARCHAR(19) NOT NULL,
    exp_date    TIMESTAMP   NOT NULL,
    cvv         VARCHAR(4)  NOT NULL,
    currency    VARCHAR(16) NOT NULL,
    balance     DECIMAL          DEFAULT 0,
    customer_id UUID REFERENCES customers (id)
);

create TABLE merchants
(
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    secret_key          VARCHAR(64)  NOT NULL,
    created_at          TIMESTAMP,
    company_recognition VARCHAR(256) NOT NULL,
    country             VARCHAR(64)  NOT NULL
);

create TABLE accounts
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    currency    VARCHAR(16) NOT NULL,
    balance     DECIMAL     NOT NULL,
    merchant_id UUID REFERENCES merchants (id)
);

create TABLE transactions
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL,
    currency         VARCHAR(16)   NOT NULL,
    amount           DECIMAL       NOT NULL,
    notification_url VARCHAR(1028) NOT NULL,
    language         VARCHAR(32)   NOT NULL,
    status           VARCHAR(64)   NOT NULL,
    type             VARCHAR(64)   NOT NULL,
    card_id          UUID REFERENCES cards (id),
    account_id       UUID REFERENCES accounts (id)
);

create TABLE webhooks
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    response_status  VARCHAR(64),
    status           VARCHAR(64)   NOT NULL,
    response_body    VARCHAR(2048),
    request_body     VARCHAR(2048),
    notification_url VARCHAR(1028) NOT NULL,
    attempt_amount   INT           NOT NULL,
    transaction_id   UUID REFERENCES transactions (id)
);