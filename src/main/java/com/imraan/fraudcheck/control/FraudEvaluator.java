package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.FraudDecision;
import com.imraan.fraudcheck.entity.FraudRulesProperties;
import com.imraan.fraudcheck.entity.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.FraudRulesProperties.RuleType;
import com.imraan.fraudcheck.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scores a transaction against hardcoded rules and maps the score to a decision.
 */
@Component
public class FraudEvaluator {

    public record Evaluation(int score, Set<String> triggeredRules) {
    }

    private final FraudRulesProperties properties;
    private final Map<String, BigDecimal> highAmountThresholds = new HashMap<>();

    public FraudEvaluator(FraudRulesProperties properties) {
        this.properties = properties;
        for (Rule rule : properties.rules()) {
            if (rule.values().isEmpty()) {
                throw new IllegalArgumentException("Rule '" + rule.code() + "' requires at least one value");
            }
            if (rule.type() == RuleType.HIGH_AMOUNT) {
                highAmountThresholds.put(rule.code(), parseAmount(rule));
            }
        }
    }

    private static BigDecimal parseAmount(Rule rule) {
        try {
            return new BigDecimal(rule.values().getFirst());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rule '" + rule.code() + "' requires a numeric value", e);
        }
    }

    public Evaluation evaluate(Transaction transaction) {
        int score = 0;
        Set<String> triggered = new LinkedHashSet<>();
        for (Rule rule : properties.rules()) {
            if (rule.enabled() && matches(rule, transaction)) {
                score += rule.riskWeight();
                triggered.add(rule.code());
            }
        }
        return new Evaluation(score, triggered);
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

    private boolean matches(Rule rule, Transaction transaction) {
        return switch (rule.type()) {
            case HIGH_AMOUNT -> transaction.getAmount().compareTo(highAmountThresholds.get(rule.code())) >= 0;
            case HIGH_RISK_COUNTRY -> containsIgnoreCase(rule.values(), transaction.getCountry());
        };
    }

    private static boolean containsIgnoreCase(List<String> values, String candidate) {
        return candidate != null
                && values.stream().anyMatch(v -> v.equalsIgnoreCase(candidate.trim()));
    }
}
