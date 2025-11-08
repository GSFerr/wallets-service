package br.com.walletservice.wallets_service.dto.response;

import br.com.walletservice.wallets_service.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionResponseDTO {
    private Long id;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private BigDecimal amount;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private String correlationId;
}

