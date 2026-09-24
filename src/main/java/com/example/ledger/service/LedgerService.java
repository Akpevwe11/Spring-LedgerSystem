package com.example.ledger.service;

import com.example.ledger.api.dto.CreateJournalEntryRequest;
import com.example.ledger.domain.JournalEntry;

import java.util.UUID;

public interface LedgerService {

    PostResult post(String idempotencyKey, CreateJournalEntryRequest request);

    JournalEntry get(UUID id);

    record PostResult(JournalEntry entry, boolean created) {
    }
}
