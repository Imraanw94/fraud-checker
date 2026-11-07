package com.imraan.fraudcheck.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "A financial transaction to be evaluated for fraud")
public record TransactionRequest(
        @Schema(description = "Identifier of the account making the transaction", example = "ACC-1001")
        @NotBlank @Size(max = 255)
        String accountId,
        @Schema(description = "Transaction amount, must be greater than zero (max 15 integer and 4 fraction digits)",
                example = "250.00", exclusiveMinimum = true, minimum = "0")
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 15, fraction = 4)
        BigDecimal amount,
        @Schema(description = "ISO 4217 currency code, exactly 3 letters", example = "USD",
                minLength = 3, maxLength = 3, pattern = "^[A-Za-z]{3}$")
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "must be a 3-letter currency code")
        String currency,
        @Schema(description = "Merchant name, max 255 characters", example = "Acme Store", nullable = true)
        @Size(max = 255)
        String merchant,
        @Schema(description = "Country where the transaction took place, max 255 characters", example = "US",
                nullable = true)
        @Size(max = 255)
        String country) {
}
