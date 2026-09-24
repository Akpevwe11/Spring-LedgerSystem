package com.example.ledger.exception;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID id) {
        super("Account not found: " + id);
    }

    public AccountNotFoundException(Collection<UUID> ids) {
        super("Account not found: " + ids.stream().map(UUID::toString).collect(Collectors.joining(", ")));
    }
}
