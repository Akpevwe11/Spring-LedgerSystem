package com.example.ledger.exception;

public class UnbalancedEntryException extends RuntimeException {

    public UnbalancedEntryException(String currency, long netMinorUnits) {
        super("Entry unbalanced for " + currency + ": net=" + netMinorUnits);
    }
}
