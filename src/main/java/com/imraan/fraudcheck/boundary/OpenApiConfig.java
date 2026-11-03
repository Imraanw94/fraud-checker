package com.imraan.fraudcheck.boundary;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI fraudCheckOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Fraud Check API")
                .version("0.0.1")
                .description("Submit financial transactions for fraud evaluation and retrieve the results."));
    }
}
