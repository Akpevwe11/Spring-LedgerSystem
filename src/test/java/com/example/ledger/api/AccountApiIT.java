package com.example.ledger.api;

import com.example.ledger.domain.AccountStatus;
import com.example.ledger.domain.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class AccountApiIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    TestRestTemplate rest;

    @Test
    void opensListsAndFetchesAccount() {
        ResponseEntity<AccountJson> created = rest.postForEntity(
                "/api/v1/accounts",
                Map.of(
                        "accountNumber", "DEP-CAROL-001",
                        "currency", "usd",
                        "type", "LIABILITY",
                        "ownerId", "carol"
                ),
                AccountJson.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().accountNumber()).isEqualTo("DEP-CAROL-001");
        assertThat(created.getBody().currency()).isEqualTo("USD");
        assertThat(created.getBody().type()).isEqualTo(AccountType.LIABILITY);
        assertThat(created.getBody().status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(created.getBody().ownerId()).isEqualTo("carol");
        assertThat(created.getBody().id()).isNotNull();

        UUID id = created.getBody().id();

        ResponseEntity<AccountJson> fetched = rest.getForEntity("/api/v1/accounts/" + id, AccountJson.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().id()).isEqualTo(id);

        ResponseEntity<AccountJson[]> listed = rest.getForEntity("/api/v1/accounts", AccountJson[].class);
        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).isNotNull();
        assertThat(listed.getBody()).extracting(AccountJson::accountNumber)
                .contains("DEP-CAROL-001", "CASH-001");
    }

    @Test
    void rejectsDuplicateAccountNumber() {
        Map<String, Object> body = Map.of(
                "accountNumber", "CASH-001",
                "currency", "USD",
                "type", "ASSET",
                "ownerId", "bank"
        );

        ResponseEntity<String> response = rest.postForEntity("/api/v1/accounts", body, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void returnsNotFoundForUnknownId() {
        ResponseEntity<String> response = rest.getForEntity(
                "/api/v1/accounts/" + UUID.randomUUID(),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    record AccountJson(
            UUID id,
            String accountNumber,
            String currency,
            AccountType type,
            AccountStatus status,
            String ownerId
    ) {
    }
}
