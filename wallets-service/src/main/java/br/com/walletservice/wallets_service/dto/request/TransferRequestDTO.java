package br.com.walletservice.wallets_service.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequestDTO {

    @NotNull(message = "Source wallet ID is required")
    private Long sourceWalletId;

    @NotNull(message = "Destination wallet ID is required")
    private Long destinationWalletId;

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;
}

