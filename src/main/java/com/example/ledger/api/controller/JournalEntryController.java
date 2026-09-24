package com.example.ledger.api.controller;

import com.example.ledger.api.docs.JournalEntryApiDocs;
import com.example.ledger.api.dto.CreateJournalEntryRequest;
import com.example.ledger.api.dto.JournalEntryResponse;
import com.example.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal-entries")
@RequiredArgsConstructor
@Tag(name = "Journal entries", description = "Post balanced double-entry journal entries")
public class JournalEntryController {

    private final LedgerService ledgerService;

    @PostMapping
    @JournalEntryApiDocs.Post
    public ResponseEntity<JournalEntryResponse> post(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateJournalEntryRequest request
    ) {
        LedgerService.PostResult result = ledgerService.post(idempotencyKey, request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(JournalEntryResponse.from(result.entry()));
    }

    @GetMapping("/{id}")
    @JournalEntryApiDocs.GetById
    public JournalEntryResponse get(@PathVariable UUID id) {
        return JournalEntryResponse.from(ledgerService.get(id));
    }
}
