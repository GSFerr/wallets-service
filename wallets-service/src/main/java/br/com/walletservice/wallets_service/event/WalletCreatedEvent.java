package br.com.walletservice.wallets_service.event;


import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletCreatedEvent {
    private String eventType = "WALLET_CREATED";
    private UUID walletId;
    private UUID userId;
    private String currency;
    private Instant occurredAt;
    private String correlationId;
}

