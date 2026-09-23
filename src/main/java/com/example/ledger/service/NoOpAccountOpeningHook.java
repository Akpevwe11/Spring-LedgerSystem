package com.example.ledger.service;

import com.example.ledger.domain.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpAccountOpeningHook implements AccountOpeningHook {

    private static final Logger log = LoggerFactory.getLogger(NoOpAccountOpeningHook.class);

    @Override
    public void onOpened(Account account) {
        log.debug("Account opened {} for owner {}", account.getAccountNumber(), account.getOwnerId());
    }
}
