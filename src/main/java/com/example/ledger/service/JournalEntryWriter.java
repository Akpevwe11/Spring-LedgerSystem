package com.example.ledger.service;

import com.example.ledger.domain.JournalEntry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.ledger.repository.JournalEntryRepository;

@Component
public class JournalEntryWriter {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntryWriter(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public JournalEntry insert(JournalEntry entry) {
        return journalEntryRepository.saveAndFlush(entry);
    }
}
