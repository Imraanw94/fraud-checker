package com.imraan.fraudcheck.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Outcome of the fraud evaluation of a transaction")
public record FraudResultResponse(
        @Schema(description = "Fraud result identifier") UUID id,
        @Schema(description = "Identifier of the evaluated transaction") UUID transactionId,
        @Schema(description = "Decision reached by the fraud rules") FraudDecision decision,
        @Schema(description = "Aggregated risk score", example = "40") int riskScore,
        @Schema(description = "Codes of the rules that were triggered") Set<String> triggeredRules,
        @Schema(description = "When the evaluation happened") Instant evaluatedAt) {

    public static FraudResultResponse from(FraudResult result) {
        return new FraudResultResponse(
                result.getId(),
                result.getTransaction().getId(),
                result.getDecision(),
                result.getRiskScore(),
                Set.copyOf(result.getTriggeredRules()),
                result.getEvaluatedAt());
    }
}
