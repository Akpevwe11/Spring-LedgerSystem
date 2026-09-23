package com.example.ledger.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void openNormalizesCurrencyAndStartsActive() {
        Account account = Account.open(" DEP-CAROL-001 ", "usd", AccountType.LIABILITY, " carol ");

        assertThat(account.getAccountNumber()).isEqualTo("DEP-CAROL-001");
        assertThat(account.getCurrency()).isEqualTo("USD");
        assertThat(account.getType()).isEqualTo(AccountType.LIABILITY);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getOwnerId()).isEqualTo("carol");
    }

    @Test
    void freezeFailsWhenClosed() {
        Account account = Account.open("DEP-CLOSED-001", "USD", AccountType.LIABILITY, "carol");
        account.close();

        assertThatThrownBy(account::freeze)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Closed");
    }
}
