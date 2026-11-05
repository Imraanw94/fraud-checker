-- Fraud rules now live in application configuration (fraud.*); results keep only the triggered rule codes.
DROP TABLE fraud_result_triggered_rules;
DROP TABLE fraud_rules;

CREATE TABLE fraud_result_triggered_rules (
    fraud_result_id UUID         NOT NULL REFERENCES fraud_results (id),
    rule_code       VARCHAR(255) NOT NULL,
    PRIMARY KEY (fraud_result_id, rule_code)
);
