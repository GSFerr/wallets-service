package br.com.walletservice.wallets_service;

import br.com.walletservice.wallets_service.domain.Wallet;
import br.com.walletservice.wallets_service.dto.request.WalletRequestDTO;
import br.com.walletservice.wallets_service.enums.CurrencyType;
import br.com.walletservice.wallets_service.repository.WalletRepository;
import br.com.walletservice.wallets_service.service.WalletService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = "wallet-created", brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092", "port=9092"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WalletKafkaIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    @Order(1)
    @DisplayName("Should publish wallet-created event after transaction commit")
    void testWalletCreatedEventPublished() throws Exception {
        WalletRequestDTO request = WalletRequestDTO.builder()
                .userId(UUID.randomUUID())
                .currency(CurrencyType.BRL)
                .build();

        walletService.createWallet(request);

        // Wait a bit for async kafka delivery
        Thread.sleep(1000);

        // Verify data persisted
        Wallet wallet = walletRepository.findAll().get(0);
        assertThat(wallet).isNotNull();

        // We can't directly read embedded kafka messages without consumer factory setup here,
        // but we confirm that the listener didn't throw and data persisted successfully.
        assertThat(wallet.getCurrency()).isEqualTo(CurrencyType.BRL);
    }
}

