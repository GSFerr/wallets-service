package br.com.walletservice.wallets_service;


import br.com.walletservice.wallets_service.domain.WalletEventLog;
import br.com.walletservice.wallets_service.event.WalletCreatedEvent;
import br.com.walletservice.wallets_service.repository.WalletEventLogRepository;
import br.com.walletservice.wallets_service.service.impl.WalletEventLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletEventLogServiceImplTest {

    @Mock
    private WalletEventLogRepository repository;

    @InjectMocks
    private WalletEventLogServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSaveWhenNotExists() {
        WalletCreatedEvent event = WalletCreatedEvent.builder()
                .walletId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .currency("BRL")
                .occurredAt(Instant.now())
                .correlationId("corr-1")
                .build();

        when(repository.existsByCorrelationId("corr-1")).thenReturn(false);
        when(repository.save(any(WalletEventLog.class))).thenAnswer(inv -> {
            WalletEventLog arg = inv.getArgument(0);
            arg.setId(UUID.randomUUID());
            return arg;
        });

        WalletEventLog saved = service.saveIfNotExists(event, "{\"ok\":true}");
        assertThat(saved).isNotNull();
        assertThat(saved.getCorrelationId()).isEqualTo("corr-1");
        verify(repository, times(1)).save(any(WalletEventLog.class));
    }

    @Test
    void shouldReturnExistingWhenExists() {
        WalletCreatedEvent event = WalletCreatedEvent.builder()
                .walletId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .currency("BRL")
                .occurredAt(Instant.now())
                .correlationId("corr-2")
                .build();

        WalletEventLog existing = WalletEventLog.builder()
                .id(UUID.randomUUID())
                .correlationId("corr-2")
                .payload("{}")
                .eventType("WALLET_CREATED")
                .eventTimestamp(Instant.now())
                .build();

        when(repository.existsByCorrelationId("corr-2")).thenReturn(true);
        when(repository.findByCorrelationId("corr-2")).thenReturn(Optional.of(existing));

        WalletEventLog result = service.saveIfNotExists(event, "{}");
        assertThat(result).isNotNull();
        assertThat(result.getCorrelationId()).isEqualTo("corr-2");
        verify(repository, never()).save(any());
    }
}

