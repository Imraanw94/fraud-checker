package com.imraan.fraudcheck.control.rule;

import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.RuleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HighAmountRule implements FraudRule {

    @Override
    public RuleType type() {
        return RuleType.HIGH_AMOUNT;
    }

    @Override
    public boolean matches(Transaction transaction, Rule config) {
        return transaction.getAmount().compareTo(parseNumericValue(config)) >= 0;
    }

    private static BigDecimal parseNumericValue(Rule config) {
        try {
            return new BigDecimal(config.values().getFirst());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rule '" + config.type().name() + "' requires a numeric value", e);
        }
    }
}
