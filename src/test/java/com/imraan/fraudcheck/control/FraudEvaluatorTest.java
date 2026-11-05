package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.FraudDecision;
import com.imraan.fraudcheck.entity.FraudRulesProperties;
import com.imraan.fraudcheck.entity.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.FraudRulesProperties.RuleType;
import com.imraan.fraudcheck.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FraudEvaluatorTest {

    private final FraudEvaluator evaluator = new FraudEvaluator(new FraudRulesProperties(40, 70, List.of(
            new Rule("HIGH_AMOUNT", RuleType.HIGH_AMOUNT,             true, 50, List.of("10000")),
                        new Rule("HIGH_RISK_COUNTRY", RuleType.HIGH_RISK_COUNTRY, true, 40, List.of("IL", "AE", "US")))));

    private Transaction tx(String amount, String country) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.setCountry(country);
        return t;
    }

    @Test
    void lowRiskTransactionIsApproved() {
        FraudEvaluator.Evaluation e = evaluator.evaluate(tx("100", "ZA"));
        assertEquals(0, e.score());
        assertEquals(Set.of(), e.triggeredRules());
        assertEquals(FraudDecision.APPROVED, evaluator.decide(e.score()));
    }

    @Test
    void highAmountIsReview() {
        FraudEvaluator.Evaluation e = evaluator.evaluate(tx("10000", "ZA"));
        assertEquals(50, e.score());
        assertEquals(Set.of("HIGH_AMOUNT"), e.triggeredRules());
        assertEquals(FraudDecision.REVIEW, evaluator.decide(e.score()));
    }

    @Test
    void highAmountAndHighRiskCountryIsRejected() {
        FraudEvaluator.Evaluation e = evaluator.evaluate(tx("20000", "US"));
        assertEquals(90, e.score());
        assertEquals(Set.of("HIGH_AMOUNT", "HIGH_RISK_COUNTRY"), e.triggeredRules());
        assertEquals(FraudDecision.REJECTED, evaluator.decide(e.score()));
    }

    @Test
    void disabledRuleIsIgnored() {
        FraudEvaluator disabled = new FraudEvaluator(new FraudRulesProperties(40, 70, List.of(
                new Rule("HIGH_AMOUNT", RuleType.HIGH_AMOUNT, false, 50, List.of("10000")))));
        assertEquals(0, disabled.evaluate(tx("20000", "ZA")).score());
    }
}
