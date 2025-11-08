package br.com.walletservice.wallets_service.domain;

import br.com.walletservice.wallets_service.enums.CurrencyType;
import br.com.walletservice.wallets_service.enums.LedgerEntryType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_wallet_created_at", columnList = "wallet_id, created_at"),
        @Index(name = "idx_ledger_idempotency", columnList = "wallet_id, idempotency_key")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LedgerEntry {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "related_wallet_id", updatable = false)
    private UUID relatedWalletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, updatable = false)
    private LedgerEntryType type;

    @Column(nullable = false, precision = 20, scale = 4, updatable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 20, scale = 4, updatable = false)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3, updatable = false)
    private CurrencyType currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "idempotency_key", length = 128, updatable = false)
    private String idempotencyKey;

    @Column(name = "correlation_id", updatable = false)
    private UUID correlationId;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
