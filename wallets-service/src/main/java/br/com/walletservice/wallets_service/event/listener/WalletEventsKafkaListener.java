package br.com.walletservice.wallets_service.event.listener;


import br.com.walletservice.wallets_service.event.WalletCreatedEvent;
import br.com.walletservice.wallets_service.service.WalletEventLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletEventsKafkaListener {

    private final ObjectMapper objectMapper;
    private final WalletEventLogService eventLogService;

    @Value("${app.kafka.topic.wallet-created:wallet-created}")
    private String walletCreatedTopic;

    @KafkaListener(topics = "${app.kafka.topic.wallet-created:wallet-created}", groupId = "wallet-service-group")
    public void onWalletCreated(String message) {
        try {
            WalletCreatedEvent event = objectMapper.readValue(message, WalletCreatedEvent.class);
            eventLogService.saveIfNotExists(event, message);

        } catch (Exception e) {
            System.err.printf("[WalletEventsKafkaListener] Failed to process message: %s, error=%s%n", message, e.getMessage());
        }
    }
}

