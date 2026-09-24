package com.example.ledger.service;

import com.example.ledger.api.dto.BalanceResponse;
import com.example.ledger.api.dto.PostingResponse;
import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountType;
import com.example.ledger.domain.EntryStatus;
import com.example.ledger.domain.Posting;
import com.example.ledger.domain.PostingDirection;
import com.example.ledger.exception.AccountNotFoundException;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.PostingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BalanceService {

    private final AccountRepository accountRepository;
    private final PostingRepository postingRepository;

    public BalanceService(AccountRepository accountRepository, PostingRepository postingRepository) {
        this.accountRepository = accountRepository;
        this.postingRepository = postingRepository;
    }

    @Transactional(readOnly = true)
    public BalanceResponse balance(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        List<Posting> postings = postingRepository.findByAccount_IdAndJournalEntry_StatusOrderByCreatedAtAsc(
                accountId,
                EntryStatus.POSTED
        );
        long minor = postings.stream()
                .mapToLong(posting -> signedAmount(account.getType(), posting))
                .sum();
        return new BalanceResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCurrency(),
                minor,
                formatMinorUnits(minor)
        );
    }

    @Transactional(readOnly = true)
    public List<PostingResponse> postings(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return postingRepository.findByAccount_IdAndJournalEntry_StatusOrderByCreatedAtAsc(
                        accountId,
                        EntryStatus.POSTED
                ).stream()
                .map(PostingResponse::from)
                .toList();
    }

    static long signedAmount(AccountType type, Posting posting) {
        boolean debitIncreases = type == AccountType.ASSET || type == AccountType.EXPENSE;
        boolean isDebit = posting.getDirection() == PostingDirection.DEBIT;
        long amount = posting.getAmountMinorUnits();
        if (debitIncreases) {
            return isDebit ? amount : -amount;
        }
        return isDebit ? -amount : amount;
    }

    static String formatMinorUnits(long minorUnits) {
        long abs = Math.abs(minorUnits);
        String formatted = (abs / 100) + "." + String.format("%02d", abs % 100);
        return minorUnits < 0 ? "-" + formatted : formatted;
    }
}
