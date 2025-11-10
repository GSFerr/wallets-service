package br.com.walletservice.wallets_service.dto.request;

import br.com.walletservice.wallets_service.enums.CurrencyType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRequestDTO {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Currency type is required")
    private CurrencyType currency;
}

