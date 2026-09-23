package com.example.ledger.exception;

public class DuplicateAccountNumberException extends RuntimeException {

    public DuplicateAccountNumberException(String accountNumber) {
        super("Account number already exists: " + accountNumber);
    }
}
