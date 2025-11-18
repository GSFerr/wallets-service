-- ============================================================
-- Flyway Migration: V1__init.sql
-- Purpose: Initial schema for Wallet Service (financial-grade)
-- ============================================================

-- ==============================
-- ENUM SIMULATION USING CHECKS
-- ==============================
-- Currency types allowed
-- We can move these to separate lookup tables later if needed.
-- BRL = Real, USD = Dollar, EUR = Euro

-- ==============================
-- TABLE: wallets
-- ==============================
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    currency VARCHAR(3) NOT NULL CHECK (currency IN ('BRL', 'USD', 'EUR')),
    balance NUMERIC(20,4) NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NULL,
    updated_by UUID NULL,
    CONSTRAINT uq_wallet_user_currency UNIQUE (user_id, currency)
);

CREATE INDEX idx_wallet_user_id ON wallets (user_id);
CREATE INDEX idx_wallet_currency ON wallets (currency);

-- ==============================
-- TABLE: ledger_entries
-- ==============================
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    related_wallet_id UUID NULL,
    type VARCHAR(32) NOT NULL CHECK (type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_DEBIT', 'TRANSFER_CREDIT', 'REVERSAL')),
    amount NUMERIC(20,4) NOT NULL CHECK (amount >= 0),
    balance_after NUMERIC(20,4) NOT NULL,
    currency VARCHAR(3) NOT NULL CHECK (currency IN ('BRL', 'USD', 'EUR')),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    idempotency_key VARCHAR(128) NULL,
    correlation_id UUID NULL,
    created_by UUID NULL,
    CONSTRAINT fk_ledger_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id)
);

CREATE INDEX idx_ledger_wallet_created_at ON ledger_entries (wallet_id, created_at);
CREATE INDEX idx_ledger_idempotency ON ledger_entries (wallet_id, idempotency_key);

-- ==============================
-- TABLE: wallet_transactions
-- ==============================
CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    type VARCHAR(32) NOT NULL CHECK (type IN ('SINGLE', 'TRANSFER')),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'COMMITTED', 'FAILED')),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    details JSONB NULL
);

CREATE INDEX idx_wallet_txn_status ON wallet_transactions (status);
CREATE INDEX idx_wallet_txn_created_at ON wallet_transactions (created_at);

-- ==============================
-- AUDIT TRAIL COMMENTARY
-- ==============================
COMMENT ON TABLE wallets IS 'Stores current balance and metadata per user and currency.';
COMMENT ON TABLE ledger_entries IS 'Immutable record of all monetary movements for audit and reconciliation.';
COMMENT ON TABLE wallet_transactions IS 'Tracks higher-level operations (e.g. transfers) linking multiple ledger entries.';

-- ==============================
-- INITIAL DATA SEED (OPTIONAL)
-- ==============================
-- INSERT INTO wallets (id, user_id, currency, balance) VALUES (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'BRL', 0);

CREATE TABLE IF NOT EXISTS wallet_event_log (
  id uuid PRIMARY KEY,
  event_type varchar(128) NOT NULL,
  payload text NOT NULL,
  correlation_id varchar(128) NOT NULL,
  event_timestamp timestamp without time zone NOT NULL,
  processed boolean NOT NULL DEFAULT false,
  processed_at timestamp without time zone,
  created_at timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT uk_wallet_event_log_correlation UNIQUE (correlation_id)
);