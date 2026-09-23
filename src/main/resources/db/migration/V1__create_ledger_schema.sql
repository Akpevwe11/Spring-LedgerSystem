CREATE TYPE account_type AS ENUM ('ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE');
CREATE TYPE account_status AS ENUM ('ACTIVE', 'FROZEN', 'CLOSED');
CREATE TYPE posting_direction AS ENUM ('DEBIT', 'CREDIT');
CREATE TYPE entry_status AS ENUM ('PENDING', 'POSTED', 'REVERSED');

CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number  VARCHAR(34) NOT NULL UNIQUE,
    currency        CHAR(3) NOT NULL,
    type            account_type NOT NULL,
    status          account_status NOT NULL DEFAULT 'ACTIVE',
    owner_id        VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE journal_entries (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference        VARCHAR(255) NOT NULL,
    description      TEXT,
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    status           entry_status NOT NULL DEFAULT 'POSTED',
    posted_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE postings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    journal_entry_id    UUID NOT NULL REFERENCES journal_entries(id),
    account_id          UUID NOT NULL REFERENCES accounts(id),
    amount_minor_units  BIGINT NOT NULL CHECK (amount_minor_units > 0),
    direction           posting_direction NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_postings_account_id ON postings(account_id);
CREATE INDEX idx_postings_journal_entry_id ON postings(journal_entry_id);
CREATE INDEX idx_journal_entries_posted_at ON journal_entries(posted_at);
