package com.example.ledger.api.dto;

import com.example.ledger.domain.PostingDirection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record PostingRequest(
        @NotNull UUID accountId,
        @NotNull @Positive Long amountMinorUnits,
        @NotNull PostingDirection direction
) {
}
