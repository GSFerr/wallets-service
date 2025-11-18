package br.com.walletservice.wallets_service.repository;

import br.com.walletservice.wallets_service.domain.WalletEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletEventLogRepository extends JpaRepository<WalletEventLog, UUID> {

    boolean existsByCorrelationId(String correlationId);

    Optional<WalletEventLog> findByCorrelationId(String correlationId);
}

