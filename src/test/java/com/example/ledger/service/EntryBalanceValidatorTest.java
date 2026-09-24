package com.example.ledger.service;

import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountType;
import com.example.ledger.domain.Posting;
import com.example.ledger.domain.PostingDirection;
import com.example.ledger.exception.UnbalancedEntryException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntryBalanceValidatorTest {

    @Test
    void acceptsBalancedUsdEntry() {
        Account cash = Account.open("CASH-001", "USD", AccountType.ASSET, "bank");
        Account deposit = Account.open("DEP-001", "USD", AccountType.LIABILITY, "alice");

        assertThatCode(() -> EntryBalanceValidator.requireBalanced(List.of(
                Posting.line(cash, 100_000, PostingDirection.DEBIT),
                Posting.line(deposit, 100_000, PostingDirection.CREDIT)
        ))).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnbalancedUsdEntry() {
        Account cash = Account.open("CASH-001", "USD", AccountType.ASSET, "bank");
        Account deposit = Account.open("DEP-001", "USD", AccountType.LIABILITY, "alice");

        assertThatThrownBy(() -> EntryBalanceValidator.requireBalanced(List.of(
                Posting.line(cash, 100_000, PostingDirection.DEBIT),
                Posting.line(deposit, 90_000, PostingDirection.CREDIT)
        ))).isInstanceOf(UnbalancedEntryException.class)
                .hasMessageContaining("USD");
    }

    @Test
    void acceptsEachCurrencyWhenIndependentlyBalanced() {
        Account usdCash = Account.open("CASH-USD", "USD", AccountType.ASSET, "bank");
        Account usdDep = Account.open("DEP-USD", "USD", AccountType.LIABILITY, "alice");
        Account eurCash = Account.open("CASH-EUR", "EUR", AccountType.ASSET, "bank");
        Account eurDep = Account.open("DEP-EUR", "EUR", AccountType.LIABILITY, "alice");

        assertThatCode(() -> EntryBalanceValidator.requireBalanced(List.of(
                Posting.line(usdCash, 100, PostingDirection.DEBIT),
                Posting.line(usdDep, 100, PostingDirection.CREDIT),
                Posting.line(eurCash, 50, PostingDirection.DEBIT),
                Posting.line(eurDep, 50, PostingDirection.CREDIT)
        ))).doesNotThrowAnyException();
    }
}
