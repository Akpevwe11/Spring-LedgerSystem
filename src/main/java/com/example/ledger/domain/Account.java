package com.example.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 34)
    private String accountNumber;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "account_type")
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "account_status")
    private AccountStatus status;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static Account open(String accountNumber, String currency, AccountType type, String ownerId) {
        Objects.requireNonNull(accountNumber, "accountNumber");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(type, "type");

        String normalizedNumber = accountNumber.trim();
        if (normalizedNumber.isEmpty() || normalizedNumber.length() > 34) {
            throw new IllegalArgumentException("accountNumber must be 1-34 characters");
        }

        String normalizedCurrency = currency.trim().toUpperCase(Locale.ROOT);
        if (normalizedCurrency.length() != 3 || !normalizedCurrency.chars().allMatch(Character::isLetter)) {
            throw new IllegalArgumentException("currency must be a 3-letter ISO code");
        }

        Account account = new Account();
        account.accountNumber = normalizedNumber;
        account.currency = normalizedCurrency;
        account.type = type;
        account.status = AccountStatus.ACTIVE;
        account.ownerId = ownerId == null || ownerId.isBlank() ? null : ownerId.trim();
        return account;
    }

    public void freeze() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Closed accounts cannot be frozen");
        }
        status = AccountStatus.FROZEN;
    }

    public void close() {
        status = AccountStatus.CLOSED;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
