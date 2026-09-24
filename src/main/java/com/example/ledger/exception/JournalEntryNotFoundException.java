package com.example.ledger.exception;

import java.util.UUID;

public class JournalEntryNotFoundException extends RuntimeException {

    public JournalEntryNotFoundException(UUID id) {
        super("Journal entry not found: " + id);
    }
}
