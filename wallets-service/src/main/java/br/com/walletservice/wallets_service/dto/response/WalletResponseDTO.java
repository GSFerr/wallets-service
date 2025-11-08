package br.com.walletservice.wallets_service.dto.response;


import br.com.walletservice.wallets_service.enums.CurrencyType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDTO {
    private Long id;
    private Long userId;
    private CurrencyType currency;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

