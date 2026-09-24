package com.example.ledger.api.controller;

import com.example.ledger.api.dto.CreateAccountRequest;
import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountStatus;
import com.example.ledger.domain.AccountType;
import com.example.ledger.exception.AccountNotFoundException;
import com.example.ledger.exception.DuplicateAccountNumberException;
import com.example.ledger.exception.GlobalExceptionHandler;
import com.example.ledger.service.AccountService;
import com.example.ledger.service.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountControllerTest {

    private FakeAccountService accountService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        accountService = new FakeAccountService();
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService, unusedBalanceService()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void openReturnsCreatedAccount() throws Exception {
        accountService.openHandler = request -> Account.open(
                request.accountNumber(),
                request.currency(),
                request.type(),
                request.ownerId()
        );

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "DEP-CAROL-001",
                                  "currency": "usd",
                                  "type": "LIABILITY",
                                  "ownerId": "carol"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("DEP-CAROL-001"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value(AccountStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.ownerId").value("carol"));
    }

    @Test
    void openRejectsDuplicateAccountNumber() throws Exception {
        accountService.openHandler = request -> {
            throw new DuplicateAccountNumberException(request.accountNumber());
        };

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountNumber": "CASH-001",
                                  "currency": "USD",
                                  "type": "ASSET",
                                  "ownerId": "bank"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void getUnknownAccountReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        accountService.getHandler = ignored -> {
            throw new AccountNotFoundException(id);
        };

        mockMvc.perform(get("/api/v1/accounts/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listReturnsAccounts() throws Exception {
        accountService.listHandler = () -> List.of(Account.open("CASH-001", "USD", AccountType.ASSET, "bank"));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("CASH-001"));
    }

    private static final class FakeAccountService implements AccountService {

        Function<CreateAccountRequest, Account> openHandler = request -> {
            throw new UnsupportedOperationException();
        };
        Function<UUID, Account> getHandler = id -> {
            throw new UnsupportedOperationException();
        };
        Supplier<List<Account>> listHandler = List::of;

        @Override
        public Account open(CreateAccountRequest request) {
            return openHandler.apply(request);
        }

        @Override
        public Account get(UUID id) {
            return getHandler.apply(id);
        }

        @Override
        public List<Account> list() {
            return listHandler.get();
        }
    }

    private static BalanceService unusedBalanceService() {
        return new BalanceService(null, null);
    }
}
