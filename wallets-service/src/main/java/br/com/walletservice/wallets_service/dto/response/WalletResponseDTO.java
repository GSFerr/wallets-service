package br.com.walletservice.wallets_service.dto.response;


import br.com.walletservice.wallets_service.enums.CurrencyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {
    private UUID id;
    private UUID userId;
    private CurrencyType currency;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;
}

