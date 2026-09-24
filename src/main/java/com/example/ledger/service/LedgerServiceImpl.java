package com.example.ledger.service;

import com.example.ledger.api.dto.CreateJournalEntryRequest;
import com.example.ledger.api.dto.PostingRequest;
import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountStatus;
import com.example.ledger.domain.JournalEntry;
import com.example.ledger.domain.Posting;
import com.example.ledger.exception.AccountNotActiveException;
import com.example.ledger.exception.AccountNotFoundException;
import com.example.ledger.exception.InvalidJournalEntryException;
import com.example.ledger.exception.JournalEntryNotFoundException;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.JournalEntryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final JournalEntryWriter journalEntryWriter;

    public LedgerServiceImpl(
            JournalEntryRepository journalEntryRepository,
            AccountRepository accountRepository,
            JournalEntryWriter journalEntryWriter
    ) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
        this.journalEntryWriter = journalEntryWriter;
    }

    @Override
    public PostResult post(String idempotencyKey, CreateJournalEntryRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidJournalEntryException("Idempotency-Key is required");
        }

        String key = idempotencyKey.trim();
        var existing = journalEntryRepository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            return new PostResult(existing.get(), false);
        }

        if (request.postings().size() < 2) {
            throw new InvalidJournalEntryException("a journal entry requires at least two postings");
        }

        Set<UUID> accountIds = request.postings().stream()
                .map(PostingRequest::accountId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<UUID, Account> accounts = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        if (accounts.size() != accountIds.size()) {
            List<UUID> missing = accountIds.stream()
                    .filter(id -> !accounts.containsKey(id))
                    .toList();
            throw new AccountNotFoundException(missing);
        }

        List<Posting> lines = new ArrayList<>();
        for (PostingRequest postingRequest : request.postings()) {
            Account account = accounts.get(postingRequest.accountId());
            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountNotActiveException(account.getAccountNumber());
            }
            lines.add(Posting.line(account, postingRequest.amountMinorUnits(), postingRequest.direction()));
        }

        EntryBalanceValidator.requireBalanced(lines);
        JournalEntry entry = JournalEntry.post(request.reference(), request.description(), key, lines);

        try {
            return new PostResult(journalEntryWriter.insert(entry), true);
        } catch (DataIntegrityViolationException ex) {
            return new PostResult(
                    journalEntryRepository.findByIdempotencyKey(key)
                            .orElseThrow(() -> ex),
                    false
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JournalEntry get(UUID id) {
        return journalEntryRepository.findDetailedById(id)
                .orElseThrow(() -> new JournalEntryNotFoundException(id));
    }
}
