package com.example.ledger.api.controller;

import com.example.ledger.api.docs.AccountApiDocs;
import com.example.ledger.api.dto.AccountResponse;
import com.example.ledger.api.dto.BalanceResponse;
import com.example.ledger.api.dto.CreateAccountRequest;
import com.example.ledger.api.dto.PostingResponse;
import com.example.ledger.service.AccountService;
import com.example.ledger.service.BalanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Open and inspect ledger accounts")
public class AccountController {

    private final AccountService accountService;
    private final BalanceService balanceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @AccountApiDocs.Open
    public AccountResponse open(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(accountService.open(request));
    }

    @GetMapping
    @AccountApiDocs.ListAll
    public List<AccountResponse> list() {
        return accountService.list().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @AccountApiDocs.GetById
    public AccountResponse get(@PathVariable UUID id) {
        return AccountResponse.from(accountService.get(id));
    }

    @GetMapping("/{id}/balance")
    @AccountApiDocs.GetBalance
    public BalanceResponse balance(@PathVariable UUID id) {
        return balanceService.balance(id);
    }

    @GetMapping("/{id}/postings")
    @AccountApiDocs.ListPostings
    public List<PostingResponse> postings(@PathVariable UUID id) {
        return balanceService.postings(id);
    }
}
