package com.imraan.fraudcheck.control.rule;

import java.util.List;

final class FraudRuleUtil {

    private FraudRuleUtil() {
    }

    static boolean containsIgnoreCase(List<String> values, String candidate) {
        return candidate != null
                && values.stream().anyMatch(v -> v.equalsIgnoreCase(candidate.trim()));
    }
}
