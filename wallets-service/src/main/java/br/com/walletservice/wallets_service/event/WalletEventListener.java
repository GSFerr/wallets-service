package br.com.walletservice.wallets_service.event;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class WalletEventListener {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String walletCreatedTopic;

    public WalletEventListener(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${app.kafka.topic.wallet-created:wallet-created}") String walletCreatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.walletCreatedTopic = walletCreatedTopic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWalletCreated(WalletCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(walletCreatedTopic, event.getWalletId().toString(), payload);
        } catch (Exception e) {
            // Log estruturado e tratativa para garantir rastreabilidade
            System.err.printf("[Kafka Publish Error] WalletCreatedEvent failed: %s%n", e.getMessage());
        }
    }
}

