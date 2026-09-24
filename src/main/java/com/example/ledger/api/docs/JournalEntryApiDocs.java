package com.example.ledger.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class JournalEntryApiDocs {

    private JournalEntryApiDocs() {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Operation(
            summary = "Post a journal entry",
            description = "Persists a balanced, immutable entry. Replay the same Idempotency-Key to return the original."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entry posted"),
            @ApiResponse(responseCode = "200", description = "Idempotent replay of an existing entry"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "422", ref = "#/components/responses/UnprocessableEntity")
    })
    public @interface Post {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Operation(summary = "Get journal entry by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entry found"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public @interface GetById {
    }
}
