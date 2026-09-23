package com.example.ledger.service;

import com.example.ledger.api.dto.CreateAccountRequest;
import com.example.ledger.domain.Account;
import com.example.ledger.exception.AccountNotFoundException;
import com.example.ledger.exception.DuplicateAccountNumberException;
import com.example.ledger.repository.AccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountOpeningHook accountOpeningHook;

    public AccountServiceImpl(AccountRepository accountRepository, AccountOpeningHook accountOpeningHook) {
        this.accountRepository = accountRepository;
        this.accountOpeningHook = accountOpeningHook;
    }

    @Override
    @Transactional
    public Account open(CreateAccountRequest request) {
        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new DuplicateAccountNumberException(request.accountNumber());
        }

        Account account = Account.open(
                request.accountNumber(),
                request.currency(),
                request.type(),
                request.ownerId()
        );

        try {
            Account saved = accountRepository.saveAndFlush(account);
            accountOpeningHook.onOpened(saved);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateAccountNumberException(request.accountNumber());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> list() {
        return accountRepository.findAll();
    }
}
