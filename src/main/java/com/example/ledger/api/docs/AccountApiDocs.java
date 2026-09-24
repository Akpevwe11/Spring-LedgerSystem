package com.example.ledger.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OpenAPI metadata for account endpoints. Keep controllers limited to HTTP mappings.
 */
public final class AccountApiDocs {

    private AccountApiDocs() {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Operation(summary = "Open an account", description = "Creates an ACTIVE account. Account numbers must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account opened"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    public @interface Open {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Operation(summary = "List accounts", description = "Returns every account in the ledger, including seed data.")
    @ApiResponse(responseCode = "200", description = "Account list")
    public @interface ListAll {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Operation(summary = "Get account by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public @interface GetById {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Operation(summary = "Get account balance", description = "Derived from POSTED postings using normal balances by account type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current balance"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public @interface GetBalance {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Operation(summary = "List account postings")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posted lines for the account"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    public @interface ListPostings {
    }
}
