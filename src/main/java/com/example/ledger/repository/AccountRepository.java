package com.example.ledger.repository;

import com.example.ledger.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByAccountNumber(String accountNumber);
}
