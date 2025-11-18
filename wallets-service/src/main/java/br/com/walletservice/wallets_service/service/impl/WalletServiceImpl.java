package br.com.walletservice.wallets_service.service.impl;

import br.com.walletservice.wallets_service.domain.Wallet;
import br.com.walletservice.wallets_service.dto.request.WalletRequestDTO;
import br.com.walletservice.wallets_service.dto.response.WalletResponseDTO;
import br.com.walletservice.wallets_service.enums.CurrencyType;
import br.com.walletservice.wallets_service.event.WalletCreatedEvent;
import br.com.walletservice.wallets_service.exception.DuplicateWalletException;
import br.com.walletservice.wallets_service.repository.WalletRepository;
import br.com.walletservice.wallets_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new wallet for the given user and currency.
     * Publishes a Kafka event only after successful DB commit.
     */
    @Override
    @Transactional
    public WalletResponseDTO createWallet(WalletRequestDTO request) {
        UUID userId = request.getUserId();
        CurrencyType currency = request.getCurrency();

        // 🔒 Business rule: prevent duplicate wallets for same user & currency
        boolean exists = walletRepository.existsByUserIdAndCurrency(userId, currency);
        if (exists) {
            throw new DuplicateWalletException(
                    String.format("Wallet already exists for user %s and currency %s", userId, currency)
            );
        }

        // 💾 Create and persist new wallet
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        // 📡 Publish domain event (captured post-commit)
        WalletCreatedEvent event = WalletCreatedEvent.builder()
                .walletId(savedWallet.getId())
                .userId(savedWallet.getUserId())
                .currency(savedWallet.getCurrency().name())
                .occurredAt(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .build();

        eventPublisher.publishEvent(event);

        // 🎯 Return DTO
        return WalletResponseDTO.builder()
                .id(savedWallet.getId())
                .userId(savedWallet.getUserId())
                .currency(savedWallet.getCurrency())
                .balance(savedWallet.getBalance())
                .createdAt(savedWallet.getCreatedAt())
                .updatedAt(savedWallet.getUpdatedAt())
                .build();
    }
}
