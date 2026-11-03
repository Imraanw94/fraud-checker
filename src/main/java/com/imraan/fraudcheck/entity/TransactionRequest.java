package com.imraan.fraudcheck.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "A financial transaction to be evaluated for fraud")
public record TransactionRequest(
        @Schema(description = "Identifier of the account making the transaction", example = "ACC-1001")
        @NotBlank String accountId,
        @Schema(description = "Transaction amount, must be greater than zero", example = "250.00")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @Schema(description = "ISO 4217 currency code", example = "USD")
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Schema(description = "Merchant name", example = "Acme Store", nullable = true)
        String merchant,
        @Schema(description = "Country where the transaction took place", example = "US", nullable = true)
        String country) {
}
