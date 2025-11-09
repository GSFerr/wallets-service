package br.com.walletservice.wallets_service;

import br.com.walletservice.wallets_service.domain.Wallet;
import br.com.walletservice.wallets_service.enums.CurrencyType;
import br.com.walletservice.wallets_service.repository.WalletRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
class ConcurrencyPessimisticLockTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("wallet_db")
            .withUsername("wallet_user")
            .withPassword("wallet_pass");

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private UUID walletId;

    @BeforeEach
    void setUp() {
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .currency(CurrencyType.BRL)
                .balance(new BigDecimal("1000.00"))
                .build();

        walletRepository.save(wallet);
        walletId = wallet.getId();
    }

    @AfterEach
    void tearDown() {
        walletRepository.deleteAll();
    }

    @Test
    @DisplayName("Should handle concurrent withdrawals safely with pessimistic lock")
    void testConcurrentWithdrawalsWithPessimisticLock() throws Exception {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    performWithdraw(walletId, new BigDecimal("100.00"));
                } catch (Exception e) {
                    System.err.println("Thread error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Wallet finalWallet = walletRepository.findById(walletId).orElseThrow();
        assertThat(finalWallet.getBalance())
                .as("Balance should reflect 5 successful withdrawals of 100.00 each")
                .isEqualByComparingTo("500.00");
    }

    public void performWithdraw(UUID walletId, BigDecimal amount) {
        transactionTemplate.executeWithoutResult(status -> {
            Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found"));

            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient funds");
            }

            wallet.setBalance(wallet.getBalance().subtract(amount));
            walletRepository.save(wallet);
        });
    }
}
