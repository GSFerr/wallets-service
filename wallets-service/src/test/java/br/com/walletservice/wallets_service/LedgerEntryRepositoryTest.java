package br.com.walletservice.wallets_service;


import br.com.walletservice.wallets_service.domain.LedgerEntry;
import br.com.walletservice.wallets_service.enums.CurrencyType;
import br.com.walletservice.wallets_service.enums.LedgerEntryType;
import br.com.walletservice.wallets_service.repository.LedgerEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class LedgerEntryRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("wallet_db")
            .withUsername("wallet_user")
            .withPassword("wallet_pass");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Test
    @DisplayName("Should persist ledger entries and retrieve history until timestamp")
    void shouldPersistAndRetrieveHistory() {
        UUID walletId = UUID.randomUUID();

        LedgerEntry e1 = LedgerEntry.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(LedgerEntryType.DEPOSIT)
                .amount(new BigDecimal("50.00"))
                .balanceAfter(new BigDecimal("50.00"))
                .currency(CurrencyType.BRL)
                .createdAt(Instant.now().minusSeconds(60))
                .build();

        LedgerEntry e2 = LedgerEntry.builder()
                .id(UUID.randomUUID())
                .walletId(walletId)
                .type(LedgerEntryType.DEPOSIT)
                .amount(new BigDecimal("25.00"))
                .balanceAfter(new BigDecimal("75.00"))
                .currency(CurrencyType.BRL)
                .createdAt(Instant.now())
                .build();

        ledgerEntryRepository.save(e1);
        ledgerEntryRepository.save(e2);

        List<LedgerEntry> all = ledgerEntryRepository.findAllByWalletId(walletId);
        assertThat(all).hasSize(2);

        // history until “between” the two events
        Instant at = Instant.now().minusSeconds(30);
        List<LedgerEntry> until = ledgerEntryRepository.findHistoryUntil(walletId, at);
        assertThat(until).hasSize(1);
        assertThat(until.get(0).getBalanceAfter()).isEqualByComparingTo("50.00");
    }
}

