package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.control.rule.BlacklistedMerchantRule;
import com.imraan.fraudcheck.control.rule.HighAmountRule;
import com.imraan.fraudcheck.control.rule.HighRiskCountryRule;
import com.imraan.fraudcheck.control.rule.UnsupportedCurrencyRule;
import com.imraan.fraudcheck.entity.Evaluation;
import com.imraan.fraudcheck.entity.enums.FraudDecision;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.RuleType;
import com.imraan.fraudcheck.entity.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FraudEvaluatorTest {

    private static FraudEvaluator evaluatorFor(FraudRulesProperties properties) {
        FraudRuleEngine engine = new FraudRuleEngine(properties, List.of(
                new HighAmountRule(),
                new HighRiskCountryRule(),
                new BlacklistedMerchantRule(),
                new UnsupportedCurrencyRule()));
        return new FraudEvaluator(properties, engine);
    }

    private final FraudEvaluator evaluator = evaluatorFor(new FraudRulesProperties(40, 70, List.of(
            new Rule(RuleType.HIGH_AMOUNT, true, 50, List.of("10000")),
            new Rule(RuleType.HIGH_RISK_COUNTRY, true, 40, List.of("IL", "AE", "US")))));

    private final FraudEvaluator merchantCurrencyEvaluator = evaluatorFor(new FraudRulesProperties(40, 70, List.of(
            new Rule(RuleType.BLACKLISTED_MERCHANT, true, 60, List.of("Shady Shop", "BadCo")),
            new Rule(RuleType.SUPPORTED_CURRENCIES, true, 30, List.of("ZAR", "USD")))));

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
        Evaluation e = evaluator.evaluateTransaction(transaction("100", "ZA", "Good Store", "ZAR"));
        assertEquals(0, e.score());
        assertEquals(Set.of(), e.triggeredRules());
        assertEquals(FraudDecision.APPROVED, evaluator.decide(e.score()));
    }

    @Test
    void highAmountIsReview() {
        Evaluation e = evaluator.evaluateTransaction(transaction("10000", "ZA", "Good Store", "ZAR"));
        assertEquals(50, e.score());
        assertEquals(Set.of("HIGH_AMOUNT"), e.triggeredRules());
        assertEquals(FraudDecision.REVIEW, evaluator.decide(e.score()));
    }

    @Test
    void highAmountAndHighRiskCountryIsRejected() {
        Evaluation e = evaluator.evaluateTransaction(transaction("20000", "US", "Good Store", "ZAR"));
        assertEquals(90, e.score());
        assertEquals(Set.of("HIGH_AMOUNT", "HIGH_RISK_COUNTRY"), e.triggeredRules());
        assertEquals(FraudDecision.REJECTED, evaluator.decide(e.score()));
    }

    @Test
    void disabledRuleIsIgnored() {
        FraudEvaluator disabled = evaluatorFor(new FraudRulesProperties(40, 70, List.of(
                new Rule(RuleType.HIGH_AMOUNT, false, 50, List.of("10000")))));
        assertEquals(0, disabled.evaluateTransaction(transaction("20000", "ZA", "Good Store", "ZAR")).score());
    }

    @Test
    void blacklistedMerchantIsTriggered() {
        Evaluation e = merchantCurrencyEvaluator.evaluateTransaction(transaction("1", "ZA", "Shady Shop", "ZAR"));
        assertEquals(60, e.score());
        assertEquals(Set.of("BLACKLISTED_MERCHANT"), e.triggeredRules());
    }

    @Test
    void blacklistedMerchantMatchIgnoresCaseAndWhitespace() {
        Evaluation e = merchantCurrencyEvaluator.evaluateTransaction(transaction("1", "ZA", "  shady shop ", "ZAR"));
        assertEquals(Set.of("BLACKLISTED_MERCHANT"), e.triggeredRules());
    }

    @Test
    void nonBlacklistedOrNullMerchantIsNotTriggered() {
        assertEquals(0, merchantCurrencyEvaluator.evaluateTransaction(transaction("1", "ZA", "Good Store", "ZAR")).score());
        assertEquals(0, merchantCurrencyEvaluator.evaluateTransaction(transaction("1", "ZA", null, "ZAR")).score());
    }

    @Test
    void supportedCurrencyIsNotTriggered() {
        assertEquals(0, merchantCurrencyEvaluator.evaluateTransaction(transaction("1", "ZA", "Good Store", "usd")).score());
    }

    @Test
    void unsupportedCurrencyIsTriggered() {
        Evaluation e = merchantCurrencyEvaluator.evaluateTransaction(transaction("1", "ZA", "Good Store", "EUR"));
        assertEquals(30, e.score());
        assertEquals(Set.of("SUPPORTED_CURRENCIES"), e.triggeredRules());
    }

    @Test
    void blacklistedMerchantAndUnsupportedCurrencyIsRejected() {
        Evaluation e = merchantCurrencyEvaluator.evaluateTransaction(transaction("1", "ZA", "BadCo", "EUR"));
        assertEquals(90, e.score());
        assertEquals(FraudDecision.REJECTED, merchantCurrencyEvaluator.decide(e.score()));
    }
}
