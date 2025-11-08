package br.com.walletservice.wallets_service.dto.response;

import br.com.walletservice.wallets_service.enums.LedgerEntryType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntryResponseDTO {
    private Long id;
    private Long walletId;
    private LedgerEntryType type;
    private BigDecimal amount;
    private String correlationId;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}

