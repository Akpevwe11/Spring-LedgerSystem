package com.example.ledger.api;

import com.example.ledger.domain.Account;
import com.example.ledger.domain.AccountType;
import com.example.ledger.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class LedgerApiIT {

    private static final UUID CASH = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private static final UUID ALICE = UUID.fromString("a0000000-0000-0000-0000-000000000002");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate rest;

    @Autowired
    AccountRepository accountRepository;

    @Test
    void postsDepositIsIdempotentAndUpdatesBalance() {
        Map<String, Object> body = depositBody(CASH, ALICE, 100_000);

        ResponseEntity<JournalJson> created = rest.exchange(
                "/api/v1/journal-entries",
                HttpMethod.POST,
                entity("deposit-alice-001", body),
                JournalJson.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        UUID entryId = created.getBody().id();
        assertThat(created.getBody().postings()).hasSize(2);

        ResponseEntity<JournalJson> replay = rest.exchange(
                "/api/v1/journal-entries",
                HttpMethod.POST,
                entity("deposit-alice-001", body),
                JournalJson.class
        );
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(replay.getBody()).isNotNull();
        assertThat(replay.getBody().id()).isEqualTo(entryId);

        ResponseEntity<BalanceJson> aliceBalance = rest.getForEntity(
                "/api/v1/accounts/" + ALICE + "/balance",
                BalanceJson.class
        );
        assertThat(aliceBalance.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(aliceBalance.getBody()).isNotNull();
        assertThat(aliceBalance.getBody().balanceMinorUnits()).isEqualTo(100_000);
        assertThat(aliceBalance.getBody().balanceFormatted()).isEqualTo("1000.00");

        ResponseEntity<BalanceJson> cashBalance = rest.getForEntity(
                "/api/v1/accounts/" + CASH + "/balance",
                BalanceJson.class
        );
        assertThat(cashBalance.getBody()).isNotNull();
        assertThat(cashBalance.getBody().balanceMinorUnits()).isEqualTo(100_000);
    }

    @Test
    void rejectsUnbalancedEntry() {
        Map<String, Object> debit = new java.util.HashMap<>();
        debit.put("accountId", CASH.toString());
        debit.put("amountMinorUnits", 100_000);
        debit.put("direction", "DEBIT");
        Map<String, Object> credit = new java.util.HashMap<>();
        credit.put("accountId", ALICE.toString());
        credit.put("amountMinorUnits", 90_000);
        credit.put("direction", "CREDIT");
        Map<String, Object> body = Map.of(
                "reference", "UNBAL",
                "postings", List.of(debit, credit)
        );

        ResponseEntity<String> response = rest.exchange(
                "/api/v1/journal-entries",
                HttpMethod.POST,
                entity("unbalanced-001", body),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void rejectsPostingToFrozenAccount() {
        Account frozen = Account.open("DEP-FROZEN-001", "USD", AccountType.LIABILITY, "frozen");
        frozen.freeze();
        frozen = accountRepository.save(frozen);

        Map<String, Object> body = depositBody(CASH, frozen.getId(), 1_000);
        ResponseEntity<String> response = rest.exchange(
                "/api/v1/journal-entries",
                HttpMethod.POST,
                entity("frozen-001", body),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private static Map<String, Object> depositBody(UUID debitAccount, UUID creditAccount, long amount) {
        return Map.of(
                "reference", "DEP-2026-001",
                "description", "Cash deposit - Alice",
                "postings", List.of(
                        Map.of(
                                "accountId", debitAccount.toString(),
                                "amountMinorUnits", amount,
                                "direction", "DEBIT"
                        ),
                        Map.of(
                                "accountId", creditAccount.toString(),
                                "amountMinorUnits", amount,
                                "direction", "CREDIT"
                        )
                )
        );
    }

    private static HttpEntity<Map<String, Object>> entity(String idempotencyKey, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);
        return new HttpEntity<>(body, headers);
    }

    record JournalJson(UUID id, String reference, String status, List<Object> postings) {
    }

    record BalanceJson(UUID accountId, String accountNumber, String currency, long balanceMinorUnits, String balanceFormatted) {
    }
}
