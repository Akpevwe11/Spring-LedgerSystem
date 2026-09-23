package com.example.ledger.service;

import com.example.ledger.api.dto.CreateAccountRequest;
import com.example.ledger.domain.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    Account open(CreateAccountRequest request);

    Account get(UUID id);

    List<Account> list();
}
