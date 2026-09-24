package com.example.ledger.service;

import com.example.ledger.domain.Posting;
import com.example.ledger.domain.PostingDirection;
import com.example.ledger.exception.UnbalancedEntryException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntryBalanceValidator {

    private EntryBalanceValidator() {
    }

    public static void requireBalanced(List<Posting> postings) {
        Map<String, Long> netByCurrency = new HashMap<>();
        for (Posting posting : postings) {
            long sign = posting.getDirection() == PostingDirection.DEBIT ? 1L : -1L;
            netByCurrency.merge(
                    posting.getAccount().getCurrency(),
                    sign * posting.getAmountMinorUnits(),
                    Long::sum
            );
        }
        netByCurrency.forEach((currency, net) -> {
            if (net != 0) {
                throw new UnbalancedEntryException(currency, net);
            }
        });
    }
}
