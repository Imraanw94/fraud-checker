package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.FraudDecision;
import com.imraan.fraudcheck.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FraudEvaluatorTest {

    private final FraudEvaluator evaluator = new FraudEvaluator();

    private Transaction tx(String amount, String country) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.setCountry(country);
        return t;
    }

    @Test
    void lowRiskTransactionIsApproved() {
        int score = evaluator.score(tx("100", "ZA"));
        assertEquals(0, score);
        assertEquals(FraudDecision.APPROVED, evaluator.decide(score));
    }

    @Test
    void highAmountIsReview() {
        int score = evaluator.score(tx("10000", "ZA"));
        assertEquals(50, score);
        assertEquals(FraudDecision.REVIEW, evaluator.decide(score));
    }

    @Test
    void highRiskCountryIsReview() {
        int score = evaluator.score(tx("100", "IL"));
        assertEquals(40, score);
        assertEquals(FraudDecision.REVIEW, evaluator.decide(score));
    }

    @Test
    void highAmountAndHighRiskCountryIsRejected() {
        int score = evaluator.score(tx("20000", "US"));
        assertEquals(90, score);
        assertEquals(FraudDecision.REJECTED, evaluator.decide(score));
    }
}
