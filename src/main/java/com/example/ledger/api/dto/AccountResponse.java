package com.example.ledger.api.dto;

import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountStatus;
import com.example.ledger.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ledger account")
public record AccountResponse(
        @Schema(description = "Account identifier", example = "a0000000-0000-0000-0000-000000000002")
        UUID id,
        @Schema(description = "Unique account number", example = "DEP-ALICE-001")
        String accountNumber,
        @Schema(description = "ISO 4217 currency code", example = "USD")
        String currency,
        AccountType type,
        AccountStatus status,
        @Schema(description = "External owner identifier", example = "alice")
        String ownerId,
        Instant createdAt,
        Instant updatedAt
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCurrency(),
                account.getType(),
                account.getStatus(),
                account.getOwnerId(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
