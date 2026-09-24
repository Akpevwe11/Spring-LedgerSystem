package com.example.ledger.api.dto;

import com.example.ledger.domain.Posting;
import com.example.ledger.domain.PostingDirection;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record PostingResponse(
        UUID id,
        UUID accountId,
        String accountNumber,
        @Schema(example = "100000") long amountMinorUnits,
        PostingDirection direction
) {

    public static PostingResponse from(Posting posting) {
        return new PostingResponse(
                posting.getId(),
                posting.getAccount().getId(),
                posting.getAccount().getAccountNumber(),
                posting.getAmountMinorUnits(),
                posting.getDirection()
        );
    }
}
