package com.example.ledger.api.dto;

import com.example.ledger.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload to open a new ledger account")
public record CreateAccountRequest(
        @Schema(description = "Unique account number", example = "DEP-CAROL-001", maxLength = 34)
        @NotBlank
        @Size(max = 34)
        String accountNumber,

        @Schema(description = "ISO 4217 currency code", example = "USD")
        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String currency,

        @Schema(description = "Accounting classification")
        @NotNull
        AccountType type,

        @Schema(description = "External owner identifier (customer or internal book)", example = "carol")
        @Size(max = 255)
        String ownerId
) {
}
