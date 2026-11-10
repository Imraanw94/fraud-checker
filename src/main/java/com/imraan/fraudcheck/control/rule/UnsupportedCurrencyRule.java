package com.imraan.fraudcheck.control.rule;

import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.RuleType;
import org.springframework.stereotype.Component;

@Component
public class UnsupportedCurrencyRule implements FraudRule {

    @Override
    public RuleType type() {
        return RuleType.SUPPORTED_CURRENCIES;
    }

    @Override
    public boolean matches(Transaction transaction, Rule config) {
        return !FraudRuleUtil.containsIgnoreCase(config.values(), transaction.getCurrency());
    }
}
