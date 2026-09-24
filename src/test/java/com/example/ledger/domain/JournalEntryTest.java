package com.example.ledger.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalEntryTest {

    @Test
    void postRequiresTwoLines() {
        Account cash = Account.open("CASH-001", "USD", AccountType.ASSET, "bank");
        assertThatThrownBy(() -> JournalEntry.post(
                "REF",
                "desc",
                "key-1",
                List.of(Posting.line(cash, 1, PostingDirection.DEBIT))
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void postSetsPostedStatus() {
        Account cash = Account.open("CASH-001", "USD", AccountType.ASSET, "bank");
        Account dep = Account.open("DEP-001", "USD", AccountType.LIABILITY, "alice");
        JournalEntry entry = JournalEntry.post(
                " DEP-1 ",
                " Cash deposit ",
                " key-1 ",
                List.of(
                        Posting.line(cash, 100, PostingDirection.DEBIT),
                        Posting.line(dep, 100, PostingDirection.CREDIT)
                )
        );

        assertThat(entry.getReference()).isEqualTo("DEP-1");
        assertThat(entry.getIdempotencyKey()).isEqualTo("key-1");
        assertThat(entry.getStatus()).isEqualTo(EntryStatus.POSTED);
        assertThat(entry.getPostings()).hasSize(2);
    }
}
