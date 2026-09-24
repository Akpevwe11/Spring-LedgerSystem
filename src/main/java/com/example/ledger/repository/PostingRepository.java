package com.example.ledger.repository;

import com.example.ledger.domain.EntryStatus;
import com.example.ledger.domain.Posting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostingRepository extends JpaRepository<Posting, UUID> {

    List<Posting> findByAccount_IdAndJournalEntry_StatusOrderByCreatedAtAsc(UUID accountId, EntryStatus status);
}
