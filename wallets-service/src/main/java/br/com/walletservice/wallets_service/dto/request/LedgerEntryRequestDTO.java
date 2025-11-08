package br.com.walletservice.wallets_service.dto.request;


import br.com.walletservice.wallets_service.enums.LedgerEntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntryRequestDTO {

    @NotNull(message = "Wallet ID is required")
    private Long walletId;

    @NotNull(message = "Type is required")
    private LedgerEntryType type;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String correlationId;
    private String idempotencyKey;
}

