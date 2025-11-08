package br.com.walletservice.wallets_service.repository;


import br.com.walletservice.wallets_service.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("SELECT le FROM LedgerEntry le WHERE le.walletId = :walletId ORDER BY le.createdAt DESC")
    List<LedgerEntry> findAllByWalletId(UUID walletId);

    @Query("SELECT le FROM LedgerEntry le WHERE le.walletId = :walletId AND le.createdAt <= :at ORDER BY le.createdAt DESC")
    List<LedgerEntry> findHistoryUntil(UUID walletId, Instant at);

    Optional<LedgerEntry> findFirstByWalletIdOrderByCreatedAtDesc(UUID walletId);
}

