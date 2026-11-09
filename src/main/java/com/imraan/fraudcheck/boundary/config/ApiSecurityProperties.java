package com.imraan.fraudcheck.boundary.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.api")
public record ApiSecurityProperties(@NotBlank String username, @NotBlank String password) {
}
