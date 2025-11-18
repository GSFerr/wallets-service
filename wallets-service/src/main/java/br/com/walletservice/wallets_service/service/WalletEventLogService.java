package br.com.walletservice.wallets_service.service;


import br.com.walletservice.wallets_service.domain.WalletEventLog;
import br.com.walletservice.wallets_service.event.WalletCreatedEvent;

public interface WalletEventLogService {

    WalletEventLog saveIfNotExists(WalletCreatedEvent event, String payload);
}

