package br.com.walletservice.wallets_service.service.impl;


import br.com.walletservice.wallets_service.domain.WalletEventLog;
import br.com.walletservice.wallets_service.event.WalletCreatedEvent;
import br.com.walletservice.wallets_service.repository.WalletEventLogRepository;
import br.com.walletservice.wallets_service.service.WalletEventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WalletEventLogServiceImpl implements WalletEventLogService {

    private final WalletEventLogRepository repository;

    @Override
    @Transactional
    public WalletEventLog saveIfNotExists(WalletCreatedEvent event, String payload) {
        String correlationId = event.getCorrelationId();
        if (correlationId == null || correlationId.isBlank()) {
            // fallback: use walletId + timestamp if correlationId missing (shouldn't happen)
            correlationId = event.getWalletId().toString() + "-" + event.getOccurredAt();
        }

        // Fast path: check existence first (reduces exception-based flow)
        if (repository.existsByCorrelationId(correlationId)) {
            return repository.findByCorrelationId(correlationId).orElse(null);
        }

        WalletEventLog log = WalletEventLog.builder()
                .eventType(event.getEventType())
                .payload(payload)
                .correlationId(correlationId)
                .eventTimestamp(event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now())
                .processed(false)
                .build();

        try {
            return repository.save(log);
        } catch (DataIntegrityViolationException ex) {
            // race condition: another processor inserted same correlationId concurrently
            return repository.findByCorrelationId(correlationId).orElseThrow(() -> ex);
        }
    }
}

