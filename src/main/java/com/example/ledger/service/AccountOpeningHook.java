package com.example.ledger.service;

import com.example.ledger.domain.Account;

/**
 * Extension point invoked after an account is persisted.
 * Production systems plug KYC / identity checks in here.
 */
public interface AccountOpeningHook {

    void onOpened(Account account);
}
