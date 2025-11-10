package br.com.walletservice.wallets_service.exception;

public class DuplicateWalletException extends RuntimeException {
    public DuplicateWalletException(String message) {
        super(message);
    }
}

