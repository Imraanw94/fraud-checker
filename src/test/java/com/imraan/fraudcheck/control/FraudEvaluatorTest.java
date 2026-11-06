package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.Evaluation;
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
            new Rule("HIGH_AMOUNT", RuleType.HIGH_AMOUNT, true, 50, List.of("10000")),
            new Rule("HIGH_RISK_COUNTRY", RuleType.HIGH_RISK_COUNTRY, true, 40, List.of("IL", "AE", "US")))));

    private final FraudEvaluator merchantCurrencyEvaluator = new FraudEvaluator(new FraudRulesProperties(40, 70, List.of(
            new Rule("BLACKLISTED_MERCHANT", RuleType.BLACKLISTED_MERCHANT, true, 60, List.of("Shady Shop", "BadCo")),
            new Rule("SUPPORTED_CURRENCIES", RuleType.SUPPORTED_CURRENCIES, true, 30, List.of("ZAR", "USD")))));

    private Transaction transaction(String amount, String country, String merchant, String currency) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.setCountry(country);
        t.setMerchant(merchant);
        t.setCurrency(currency);
        return t;
    }

    @Test
    void lowRiskTransactionIsApproved() {
        Evaluation e = evaluator.evaluate(transaction("100", "ZA", "Good Store", "ZAR"));
        assertEquals(0, e.score());
        assertEquals(Set.of(), e.triggeredRules());
        assertEquals(FraudDecision.APPROVED, evaluator.decide(e.score()));
    }

    @Test
    void highAmountIsReview() {
        Evaluation e = evaluator.evaluate(transaction("10000", "ZA", "Good Store", "ZAR"));
        assertEquals(50, e.score());
        assertEquals(Set.of("HIGH_AMOUNT"), e.triggeredRules());
        assertEquals(FraudDecision.REVIEW, evaluator.decide(e.score()));
    }

    @Test
    void highAmountAndHighRiskCountryIsRejected() {
        Evaluation e = evaluator.evaluate(transaction("20000", "US", "Good Store", "ZAR"));
        assertEquals(90, e.score());
        assertEquals(Set.of("HIGH_AMOUNT", "HIGH_RISK_COUNTRY"), e.triggeredRules());
        assertEquals(FraudDecision.REJECTED, evaluator.decide(e.score()));
    }

    @Test
    void disabledRuleIsIgnored() {
        FraudEvaluator disabled = new FraudEvaluator(new FraudRulesProperties(40, 70, List.of(
                new Rule("HIGH_AMOUNT", RuleType.HIGH_AMOUNT, false, 50, List.of("10000")))));
        assertEquals(0, disabled.evaluate(transaction("20000", "ZA", "Good Store", "ZAR")).score());
    }

    @Test
    void blacklistedMerchantIsTriggered() {
        Evaluation e = merchantCurrencyEvaluator.evaluate(transaction("1", "ZA", "Shady Shop", "ZAR"));
        assertEquals(60, e.score());
        assertEquals(Set.of("BLACKLISTED_MERCHANT"), e.triggeredRules());
    }

    @Test
    void blacklistedMerchantMatchIgnoresCaseAndWhitespace() {
        Evaluation e = merchantCurrencyEvaluator.evaluate(transaction("1", "ZA", "  shady shop ", "ZAR"));
        assertEquals(Set.of("BLACKLISTED_MERCHANT"), e.triggeredRules());
    }

    @Test
    void nonBlacklistedOrNullMerchantIsNotTriggered() {
        assertEquals(0, merchantCurrencyEvaluator.evaluate(transaction("1", "ZA", "Good Store", "ZAR")).score());
        assertEquals(0, merchantCurrencyEvaluator.evaluate(transaction("1", "ZA", null, "ZAR")).score());
    }

    @Test
    void supportedCurrencyIsNotTriggered() {
        assertEquals(0, merchantCurrencyEvaluator.evaluate(transaction("1", "ZA", "Good Store", "usd")).score());
    }

    @Test
    void unsupportedCurrencyIsTriggered() {
        Evaluation e = merchantCurrencyEvaluator.evaluate(transaction("1", "ZA", "Good Store", "EUR"));
        assertEquals(30, e.score());
        assertEquals(Set.of("SUPPORTED_CURRENCIES"), e.triggeredRules());
    }

    @Test
    void blacklistedMerchantAndUnsupportedCurrencyIsRejected() {
        Evaluation e = merchantCurrencyEvaluator.evaluate(transaction("1", "ZA", "BadCo", "EUR"));
        assertEquals(90, e.score());
        assertEquals(FraudDecision.REJECTED, merchantCurrencyEvaluator.decide(e.score()));
    }
}
