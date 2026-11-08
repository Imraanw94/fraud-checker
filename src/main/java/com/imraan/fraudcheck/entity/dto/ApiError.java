package com.imraan.fraudcheck.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Consistent error response returned for failed requests")
public record ApiError(
        @Schema(example = "2026-01-01T12:00:00Z") Instant timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "Validation failed") String message,
        @Schema(description = "Per-field validation failures; empty when not applicable") List<FieldViolation> violations) {

    @Schema(description = "A single field validation failure")
    public record FieldViolation(
            @Schema(example = "amount") String field,
            @Schema(example = "must be greater than 0") String message) {
    }
}
