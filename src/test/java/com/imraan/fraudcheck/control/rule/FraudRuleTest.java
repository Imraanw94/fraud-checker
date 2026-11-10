package com.imraan.fraudcheck.control.rule;

import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.Rule;
import com.imraan.fraudcheck.entity.config.FraudRulesProperties.RuleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudRuleTest {

    private static Transaction transaction(String amount, String country, String merchant, String currency) {
        Transaction t = new Transaction();
        t.setAmount(new BigDecimal(amount));
        t.setCountry(country);
        t.setMerchant(merchant);
        t.setCurrency(currency);
        return t;
    }

    private static Rule config(RuleType type, String... values) {
        return new Rule(type, true, 10, List.of(values));
    }

    @Test
    void highAmountMatchesAtOrAboveThreshold() {
        Rule config = config(RuleType.HIGH_AMOUNT, "10000");
        HighAmountRule rule = new HighAmountRule();
        assertEquals(RuleType.HIGH_AMOUNT, rule.type());
        assertTrue(rule.matches(transaction("10000", "ZA", "m", "ZAR"), config));
        assertFalse(rule.matches(transaction("9999.99", "ZA", "m", "ZAR"), config));
    }

    @Test
    void highAmountRejectsNonNumericThreshold() {
        Rule config = config(RuleType.HIGH_AMOUNT, "abc");
        assertThrows(IllegalArgumentException.class,
                () -> new HighAmountRule().matches(transaction("1", "ZA", "m", "ZAR"), config));
    }

    @Test
    void highRiskCountryMatchesIgnoringCase() {
        Rule config = config(RuleType.HIGH_RISK_COUNTRY, "US", "IL");
        HighRiskCountryRule rule = new HighRiskCountryRule();
        assertEquals(RuleType.HIGH_RISK_COUNTRY, rule.type());
        assertTrue(rule.matches(transaction("1", " us ", "m", "ZAR"), config));
        assertFalse(rule.matches(transaction("1", "ZA", "m", "ZAR"), config));
        assertFalse(rule.matches(transaction("1", null, "m", "ZAR"), config));
    }

    @Test
    void blacklistedMerchantMatchesIgnoringCaseAndWhitespace() {
        Rule config = config(RuleType.BLACKLISTED_MERCHANT, "Shady Shop");
        BlacklistedMerchantRule rule = new BlacklistedMerchantRule();
        assertEquals(RuleType.BLACKLISTED_MERCHANT, rule.type());
        assertTrue(rule.matches(transaction("1", "ZA", "  shady shop ", "ZAR"), config));
        assertFalse(rule.matches(transaction("1", "ZA", "Good Store", "ZAR"), config));
        assertFalse(rule.matches(transaction("1", "ZA", null, "ZAR"), config));
    }

    @Test
    void unsupportedCurrencyMatchesOnlyWhenNotSupported() {
        Rule config = config(RuleType.SUPPORTED_CURRENCIES, "ZAR", "USD");
        UnsupportedCurrencyRule rule = new UnsupportedCurrencyRule();
        assertEquals(RuleType.SUPPORTED_CURRENCIES, rule.type());
        assertTrue(rule.matches(transaction("1", "ZA", "m", "EUR"), config));
        assertFalse(rule.matches(transaction("1", "ZA", "m", "usd"), config));
    }
}
