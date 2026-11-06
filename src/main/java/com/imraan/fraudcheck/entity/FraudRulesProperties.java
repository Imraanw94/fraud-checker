package com.imraan.fraudcheck.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties("fraudrules")
public record FraudRulesProperties(
        @Min(0) int reviewThreshold,
        @Min(0) int rejectThreshold,
        @DefaultValue List<@Valid Rule> rules) {

    public enum RuleType {
        HIGH_AMOUNT,
        HIGH_RISK_COUNTRY,
        BLACKLISTED_MERCHANT,
        SUPPORTED_CURRENCIES
    }

    public record Rule(
            @NotBlank String code,
            @NotNull RuleType type,
            @DefaultValue("true") boolean enabled,
            @Min(0) int riskWeight,
            @DefaultValue List<String> values) {
    }
}
