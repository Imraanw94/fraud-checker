package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.FraudDecision;
import com.imraan.fraudcheck.entity.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Scores a transaction against hardcoded rules and maps the score to a decision.
 */
@Component
public class FraudEvaluator {

    static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    static final int HIGH_AMOUNT_SCORE = 50;
    static final int HIGH_RISK_COUNTRY_SCORE = 40;
    static final int REVIEW_THRESHOLD = 40;
    static final int REJECT_THRESHOLD = 70;

    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("IL", "AE", "US");

    public int score(Transaction transaction) {
        int score = 0;
        if (transaction.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            score += HIGH_AMOUNT_SCORE;
        }
        if (transaction.getCountry() != null
                && HIGH_RISK_COUNTRIES.contains(transaction.getCountry().toUpperCase(java.util.Locale.ROOT))) {
            score += HIGH_RISK_COUNTRY_SCORE;
        }
        return score;
    }

    public FraudDecision decide(int score) {
        if (score >= REJECT_THRESHOLD) {
            return FraudDecision.REJECTED;
        }
        if (score >= REVIEW_THRESHOLD) {
            return FraudDecision.REVIEW;
        }
        return FraudDecision.APPROVED;
    }
}
