CREATE TABLE transactions (
    id         UUID PRIMARY KEY,
    account_id VARCHAR(255)             NOT NULL,
    amount     NUMERIC(19, 4)           NOT NULL,
    currency   VARCHAR(3)               NOT NULL,
    merchant   VARCHAR(255),
    country    VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_transactions_account_id ON transactions (account_id);

CREATE TABLE fraud_rules (
    id          UUID PRIMARY KEY,
    code        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    risk_weight INTEGER      NOT NULL,
    enabled     BOOLEAN      NOT NULL
);

CREATE TABLE fraud_results (
    id             UUID PRIMARY KEY,
    transaction_id UUID                     NOT NULL UNIQUE REFERENCES transactions (id),
    decision       VARCHAR(255)             NOT NULL,
    risk_score     INTEGER                  NOT NULL,
    evaluated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE fraud_result_triggered_rules (
    fraud_result_id UUID NOT NULL REFERENCES fraud_results (id),
    fraud_rule_id   UUID NOT NULL REFERENCES fraud_rules (id),
    PRIMARY KEY (fraud_result_id, fraud_rule_id)
);
