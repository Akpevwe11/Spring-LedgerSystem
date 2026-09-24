package com.example.ledger.exception;

public class InvalidJournalEntryException extends RuntimeException {

    public InvalidJournalEntryException(String message) {
        super(message);
    }
}
