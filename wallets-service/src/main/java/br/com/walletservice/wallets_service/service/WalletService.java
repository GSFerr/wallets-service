package br.com.walletservice.wallets_service.service;

import br.com.walletservice.wallets_service.dto.request.WalletRequestDTO;
import br.com.walletservice.wallets_service.dto.response.WalletResponseDTO;
import org.springframework.stereotype.Service;

public interface WalletService {

    WalletResponseDTO createWallet(WalletRequestDTO request);

}

