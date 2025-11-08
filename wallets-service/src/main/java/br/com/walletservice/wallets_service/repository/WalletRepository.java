package br.com.walletservice.wallets_service.repository;


import br.com.walletservice.wallets_service.domain.Wallet;
import br.com.walletservice.wallets_service.enums.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUserIdAndCurrency(UUID userId, CurrencyType currency);

    boolean existsByUserIdAndCurrency(UUID userId, CurrencyType currency);
}

