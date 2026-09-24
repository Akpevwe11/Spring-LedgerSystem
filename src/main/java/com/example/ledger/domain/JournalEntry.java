package com.example.ledger.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "journal_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalEntry {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String reference;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "entry_status")
    private EntryStatus status;

    @Column(name = "posted_at", nullable = false)
    private Instant postedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Posting> postings = new ArrayList<>();

    public static JournalEntry post(
            String reference,
            String description,
            String idempotencyKey,
            List<Posting> lines
    ) {
        Objects.requireNonNull(reference, "reference");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(lines, "postings");

        String normalizedReference = reference.trim();
        String normalizedKey = idempotencyKey.trim();
        if (normalizedReference.isEmpty()) {
            throw new IllegalArgumentException("reference must not be blank");
        }
        if (normalizedKey.isEmpty()) {
            throw new IllegalArgumentException("idempotencyKey must not be blank");
        }
        if (lines.size() < 2) {
            throw new IllegalArgumentException("a journal entry requires at least two postings");
        }

        JournalEntry entry = new JournalEntry();
        entry.reference = normalizedReference;
        entry.description = description == null || description.isBlank() ? null : description.trim();
        entry.idempotencyKey = normalizedKey;
        entry.status = EntryStatus.POSTED;
        for (Posting line : lines) {
            Objects.requireNonNull(line, "posting");
            line.attachTo(entry);
            entry.postings.add(line);
        }
        return entry;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (postedAt == null) {
            postedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
