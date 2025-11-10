package br.com.walletservice.wallets_service.service.impl;

import br.com.walletservice.wallets_service.domain.Wallet;
import br.com.walletservice.wallets_service.dto.request.WalletRequestDTO;
import br.com.walletservice.wallets_service.dto.response.WalletResponseDTO;
import br.com.walletservice.wallets_service.enums.CurrencyType;
import br.com.walletservice.wallets_service.exception.DuplicateWalletException;
import br.com.walletservice.wallets_service.repository.WalletRepository;
import br.com.walletservice.wallets_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public WalletResponseDTO createWallet(WalletRequestDTO request) {
        UUID userId = request.getUserId();
        CurrencyType currency = request.getCurrency();

        // Business rule: prevent duplicate wallets for the same user & currency
        boolean exists = walletRepository.existsByUserIdAndCurrency(userId, currency);
        if (exists) {
            throw new DuplicateWalletException(
                    String.format("Wallet already exists for user %s and currency %s", userId, currency)
            );
        }

        // Create and persist new wallet
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        // Return DTO
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
