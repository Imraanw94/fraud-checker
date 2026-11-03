package com.imraan.fraudcheck.entity;

import java.time.Instant;
import java.util.UUID;

public record FraudResultResponse(
        UUID id,
        UUID transactionId,
        FraudDecision decision,
        int riskScore,
        Instant evaluatedAt) {

    public static FraudResultResponse from(FraudResult result) {
        return new FraudResultResponse(
                result.getId(),
                result.getTransaction().getId(),
                result.getDecision(),
                result.getRiskScore(),
                result.getEvaluatedAt());
    }
}
