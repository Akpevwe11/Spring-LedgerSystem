package com.example.ledger.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI ledgerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ledger System API")
                        .description("Account management and double-entry ledger operations")
                        .version("v1"))
                .components(new Components()
                        .addResponses("BadRequest", problem("Request validation failed"))
                        .addResponses("NotFound", problem("Resource not found"))
                        .addResponses("Conflict", problem("Resource already exists"))
                        .addResponses("UnprocessableEntity", problem("Business rule failed")));
    }

    private static ApiResponse problem(String description) {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/ProblemDetail");
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                        new MediaType().schema(schema)));
    }
}
