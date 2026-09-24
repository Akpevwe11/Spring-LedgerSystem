package com.example.ledger.api.dto;

import com.example.ledger.domain.EntryStatus;
import com.example.ledger.domain.JournalEntry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id,
        String reference,
        String description,
        String idempotencyKey,
        EntryStatus status,
        Instant postedAt,
        List<PostingResponse> postings
) {

    public static JournalEntryResponse from(JournalEntry entry) {
        return new JournalEntryResponse(
                entry.getId(),
                entry.getReference(),
                entry.getDescription(),
                entry.getIdempotencyKey(),
                entry.getStatus(),
                entry.getPostedAt(),
                entry.getPostings().stream().map(PostingResponse::from).toList()
        );
    }
}
