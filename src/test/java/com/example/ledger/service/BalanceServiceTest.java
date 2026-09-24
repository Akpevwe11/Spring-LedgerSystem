package com.example.ledger.service;

import com.example.ledger.domain.AccountType;
import com.example.ledger.domain.Posting;
import com.example.ledger.domain.PostingDirection;
import com.example.ledger.domain.Account;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceServiceTest {

    @Test
    void assetDebitIncreasesBalance() {
        Account cash = Account.open("CASH-001", "USD", AccountType.ASSET, "bank");
        Posting debit = Posting.line(cash, 100_000, PostingDirection.DEBIT);
        Posting credit = Posting.line(cash, 20_000, PostingDirection.CREDIT);

        assertThat(BalanceService.signedAmount(AccountType.ASSET, debit)).isEqualTo(100_000);
        assertThat(BalanceService.signedAmount(AccountType.ASSET, credit)).isEqualTo(-20_000);
    }

    @Test
    void liabilityCreditIncreasesBalance() {
        Account deposit = Account.open("DEP-001", "USD", AccountType.LIABILITY, "alice");
        Posting credit = Posting.line(deposit, 100_000, PostingDirection.CREDIT);
        Posting debit = Posting.line(deposit, 20_000, PostingDirection.DEBIT);

        assertThat(BalanceService.signedAmount(AccountType.LIABILITY, credit)).isEqualTo(100_000);
        assertThat(BalanceService.signedAmount(AccountType.LIABILITY, debit)).isEqualTo(-20_000);
    }

    @Test
    void formatsMinorUnits() {
        assertThat(BalanceService.formatMinorUnits(80000)).isEqualTo("800.00");
        assertThat(BalanceService.formatMinorUnits(-50)).isEqualTo("-0.50");
    }
}
