package com.example.ledger.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateJournalEntryRequest(
        @NotBlank @Size(max = 255) String reference,
        String description,
        @NotEmpty @Valid List<PostingRequest> postings
) {
}
