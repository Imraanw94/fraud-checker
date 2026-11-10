package com.imraan.fraudcheck.control.rule;

import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.RuleType;

public interface FraudRule {

    RuleType type();

    boolean matches(Transaction transaction, Rule config);

}
