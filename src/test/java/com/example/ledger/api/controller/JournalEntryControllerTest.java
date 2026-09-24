package com.example.ledger.api.controller;

import com.example.ledger.api.dto.CreateJournalEntryRequest;
import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountType;
import com.example.ledger.domain.JournalEntry;
import com.example.ledger.domain.Posting;
import com.example.ledger.domain.PostingDirection;
import com.example.ledger.exception.GlobalExceptionHandler;
import com.example.ledger.exception.UnbalancedEntryException;
import com.example.ledger.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JournalEntryControllerTest {

    private FakeLedgerService ledgerService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ledgerService = new FakeLedgerService();
        mockMvc = MockMvcBuilders.standaloneSetup(new JournalEntryController(ledgerService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void postsEntry() throws Exception {
        Account cash = Account.open("CASH-001", "USD", AccountType.ASSET, "bank");
        Account dep = Account.open("DEP-ALICE-001", "USD", AccountType.LIABILITY, "alice");
        JournalEntry entry = JournalEntry.post(
                "DEP-2026-001",
                "Cash deposit - Alice",
                "deposit-alice-001",
                List.of(
                        Posting.line(cash, 100_000, PostingDirection.DEBIT),
                        Posting.line(dep, 100_000, PostingDirection.CREDIT)
                )
        );

        ledgerService.postHandler = (key, request) -> new LedgerService.PostResult(entry, true);

        mockMvc.perform(post("/api/v1/journal-entries")
                        .header("Idempotency-Key", "deposit-alice-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reference": "DEP-2026-001",
                                  "description": "Cash deposit - Alice",
                                  "postings": [
                                    {"accountId": "a0000000-0000-0000-0000-000000000001", "amountMinorUnits": 100000, "direction": "DEBIT"},
                                    {"accountId": "a0000000-0000-0000-0000-000000000002", "amountMinorUnits": 100000, "direction": "CREDIT"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference").value("DEP-2026-001"))
                .andExpect(jsonPath("$.status").value("POSTED"));
    }

    @Test
    void replayReturnsOk() throws Exception {
        Account cash = Account.open("CASH-001", "USD", AccountType.ASSET, "bank");
        Account dep = Account.open("DEP-ALICE-001", "USD", AccountType.LIABILITY, "alice");
        JournalEntry entry = JournalEntry.post(
                "DEP-2026-001",
                "Cash deposit - Alice",
                "deposit-alice-001",
                List.of(
                        Posting.line(cash, 100_000, PostingDirection.DEBIT),
                        Posting.line(dep, 100_000, PostingDirection.CREDIT)
                )
        );
        ledgerService.postHandler = (key, request) -> new LedgerService.PostResult(entry, false);

        mockMvc.perform(post("/api/v1/journal-entries")
                        .header("Idempotency-Key", "deposit-alice-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reference": "DEP-2026-001",
                                  "postings": [
                                    {"accountId": "a0000000-0000-0000-0000-000000000001", "amountMinorUnits": 100000, "direction": "DEBIT"},
                                    {"accountId": "a0000000-0000-0000-0000-000000000002", "amountMinorUnits": 100000, "direction": "CREDIT"}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void unbalancedReturnsUnprocessable() throws Exception {
        ledgerService.postHandler = (key, request) -> {
            throw new UnbalancedEntryException("USD", 1000);
        };

        mockMvc.perform(post("/api/v1/journal-entries")
                        .header("Idempotency-Key", "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reference": "BAD",
                                  "postings": [
                                    {"accountId": "a0000000-0000-0000-0000-000000000001", "amountMinorUnits": 100000, "direction": "DEBIT"},
                                    {"accountId": "a0000000-0000-0000-0000-000000000002", "amountMinorUnits": 90000, "direction": "CREDIT"}
                                  ]
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    private static final class FakeLedgerService implements LedgerService {

        BiFunction<String, CreateJournalEntryRequest, PostResult> postHandler = (key, request) -> {
            throw new UnsupportedOperationException();
        };

        @Override
        public PostResult post(String idempotencyKey, CreateJournalEntryRequest request) {
            return postHandler.apply(idempotencyKey, request);
        }

        @Override
        public JournalEntry get(UUID id) {
            throw new UnsupportedOperationException();
        }
    }
}
