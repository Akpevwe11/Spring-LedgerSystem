package com.example.ledger.api.controller;

import com.example.ledger.api.dto.AccountResponse;
import com.example.ledger.api.dto.CreateAccountRequest;
import com.example.ledger.service.AccountService;
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
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse open(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(accountService.open(request));
    }

    @GetMapping
    public List<AccountResponse> list() {
        return accountService.list().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) {
        return AccountResponse.from(accountService.get(id));
    }
}
