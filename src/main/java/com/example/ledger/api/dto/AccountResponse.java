package com.example.ledger.api.dto;

import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountStatus;
import com.example.ledger.domain.AccountType;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String currency,
        AccountType type,
        AccountStatus status,
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
