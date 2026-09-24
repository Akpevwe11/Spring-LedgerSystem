package com.example.ledger.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record BalanceResponse(
        UUID accountId,
        String accountNumber,
        String currency,
        @Schema(example = "80000") long balanceMinorUnits,
        @Schema(example = "800.00") String balanceFormatted
) {
}
