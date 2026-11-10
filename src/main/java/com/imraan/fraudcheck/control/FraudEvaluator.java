package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.Evaluation;
import com.imraan.fraudcheck.entity.FraudDecision;
import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties;
import org.springframework.stereotype.Component;

@Component
public class FraudEvaluator {

    private final FraudRulesProperties properties;
    private final FraudRuleEngine ruleEngine;

    public FraudEvaluator(FraudRulesProperties properties, FraudRuleEngine ruleEngine) {
        this.properties = properties;
        this.ruleEngine = ruleEngine;
    }

    public Evaluation evaluateTransaction(Transaction transaction) {
        return ruleEngine.evaluateTransaction(transaction);
    }

    public FraudDecision decide(int score) {
        if (score >= properties.rejectThreshold()) {
            return FraudDecision.REJECTED;
        }
        if (score >= properties.reviewThreshold()) {
            return FraudDecision.REVIEW;
        }
        return FraudDecision.APPROVED;
    }
}
