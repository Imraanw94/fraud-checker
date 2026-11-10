package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.control.rule.HighAmountRule;
import com.imraan.fraudcheck.control.rule.HighRiskCountryRule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.RuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FraudRuleEngineTest {

    private static FraudRulesProperties props(Rule... rules) {
        return new FraudRulesProperties(40, 70, List.of(rules));
    }

    @Test
    void rejectsRuleWithoutValues() {
        FraudRulesProperties p = props(new Rule(RuleType.HIGH_AMOUNT, true, 50, List.of()));
        List<com.imraan.fraudcheck.control.rule.FraudRule> rules = List.of(new HighAmountRule());
        assertThrows(IllegalArgumentException.class, () -> new FraudRuleEngine(p, rules));
    }

    @Test
    void rejectsRuleTypeWithoutImplementation() {
        FraudRulesProperties p = props(new Rule(RuleType.HIGH_AMOUNT, true, 50, List.of("1")));
        List<com.imraan.fraudcheck.control.rule.FraudRule> rules = List.of(new HighRiskCountryRule());
        assertThrows(IllegalArgumentException.class, () -> new FraudRuleEngine(p, rules));
    }
}
