package com.example.ledger.api.dto;

import com.example.ledger.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank
        @Size(max = 34)
        String accountNumber,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String currency,

        @NotNull
        AccountType type,

        @Size(max = 255)
        String ownerId
) {
}
