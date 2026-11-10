-- Indexes supporting SearchService transaction and fraud alert queries. Schema only; no data or behavior change.

-- accountId equality + createdAt range/sort. Supersedes the single-column account_id index (leading column).
DROP INDEX idx_transactions_account_id;
CREATE INDEX idx_transactions_account_id_created_at ON transactions (account_id, created_at);

-- createdAt range filter and default sort without an accountId filter.
CREATE INDEX idx_transactions_created_at ON transactions (created_at);

-- The search compares upper(country), so the index must use the same expression.
CREATE INDEX idx_transactions_country_upper ON transactions (upper(country));

-- decision equality + evaluatedAt range/sort.
CREATE INDEX idx_fraud_results_decision_evaluated_at ON fraud_results (decision, evaluated_at);

-- evaluatedAt range filter and default sort without a decision filter.
CREATE INDEX idx_fraud_results_evaluated_at ON fraud_results (evaluated_at);

-- minRiskScore range filter and riskScore sort.
CREATE INDEX idx_fraud_results_risk_score ON fraud_results (risk_score);
