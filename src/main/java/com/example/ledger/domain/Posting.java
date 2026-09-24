package com.example.ledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "postings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting {

    @Id
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "amount_minor_units", nullable = false)
    private Long amountMinorUnits;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "posting_direction")
    private PostingDirection direction;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static Posting line(Account account, long amountMinorUnits, PostingDirection direction) {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(direction, "direction");
        if (amountMinorUnits <= 0) {
            throw new IllegalArgumentException("amountMinorUnits must be greater than 0");
        }

        Posting posting = new Posting();
        posting.account = account;
        posting.amountMinorUnits = amountMinorUnits;
        posting.direction = direction;
        return posting;
    }

    void attachTo(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
