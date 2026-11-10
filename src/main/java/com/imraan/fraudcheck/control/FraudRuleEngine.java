package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.control.rule.FraudRule;
import com.imraan.fraudcheck.entity.Evaluation;
import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.RuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Orchestrates rule execution: runs each enabled configured rule through its {@link FraudRule}
 * implementation and aggregates score and triggered rule codes.
 */
@Component
public class FraudRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(FraudRuleEngine.class);

    private final FraudRulesProperties properties;
    private final List<FraudRule> fraudRules;

    public FraudRuleEngine(FraudRulesProperties properties, List<FraudRule> fraudRules) {
        this.properties = properties;
        this.fraudRules = List.copyOf(fraudRules);

        for (Rule config : properties.rules()) {
            validateRule(config);
        }
    }

    public Evaluation evaluateTransaction(Transaction transaction) {
        int score = 0;
        Set<String> triggered = new LinkedHashSet<>();
        for (Rule config : properties.rules()) {
            if (config.enabled()) {
                for (FraudRule rule : fraudRules) {
                    if (rule.type() == config.type()) {
                        if (rule.matches(transaction, config)) {
                            score += config.riskWeight();
                            triggered.add(config.type().name());
                            log.debug("Fraud rule triggered: transactionId={} rule={} weight={}",
                                    transaction.getId(), config.type(), config.riskWeight());
                        }
                        break;
                    }
                }
            }
        }
        log.debug("Fraud rules evaluated: transactionId={} score={} triggeredRules={}",
                transaction.getId(), score, triggered);
        return new Evaluation(score, triggered);
    }

    private void validateRule(Rule config) {
        if (config.values().isEmpty()) {
            throw new IllegalArgumentException("Rule '" + config.type().name() + "' requires at least one value");
        }
        if (fraudRules.stream().noneMatch(rule -> rule.type() == config.type())) {
            throw new IllegalArgumentException("No implementation for rule type " + config.type());
        }
    }
}
