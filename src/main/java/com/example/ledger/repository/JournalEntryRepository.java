package com.example.ledger.repository;

import com.example.ledger.domain.JournalEntry;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    @EntityGraph(attributePaths = {"postings", "postings.account"})
    Optional<JournalEntry> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = {"postings", "postings.account"})
    @Query("select je from JournalEntry je where je.id = :id")
    Optional<JournalEntry> findDetailedById(UUID id);
}
