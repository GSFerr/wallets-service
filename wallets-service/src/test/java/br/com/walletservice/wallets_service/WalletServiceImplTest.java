package br.com.walletservice.wallets_service;

import br.com.walletservice.wallets_service.domain.Wallet;
import br.com.walletservice.wallets_service.dto.request.WalletRequestDTO;
import br.com.walletservice.wallets_service.dto.response.WalletResponseDTO;
import br.com.walletservice.wallets_service.enums.CurrencyType;
import br.com.walletservice.wallets_service.event.WalletCreatedEvent;
import br.com.walletservice.wallets_service.exception.DuplicateWalletException;
import br.com.walletservice.wallets_service.repository.WalletRepository;
import br.com.walletservice.wallets_service.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID userId;
    private WalletRequestDTO request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        request = WalletRequestDTO.builder()
                .userId(userId)
                .currency(CurrencyType.BRL)
                .build();
    }

    @Test
    @DisplayName("Should create wallet successfully and publish event when not exists")
    void shouldCreateWalletSuccessfullyAndPublishEvent() {
        // given
        when(walletRepository.existsByUserIdAndCurrency(userId, CurrencyType.BRL)).thenReturn(false);

        Wallet savedWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .currency(CurrencyType.BRL)
                .balance(BigDecimal.ZERO)
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // when
        WalletResponseDTO response = walletService.createWallet(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getCurrency()).isEqualTo(CurrencyType.BRL);
        assertThat(response.getBalance()).isEqualByComparingTo("0.00");

        // verify repository behavior
        verify(walletRepository, times(1)).existsByUserIdAndCurrency(userId, CurrencyType.BRL);
        verify(walletRepository, times(1)).save(any(Wallet.class));

        // verify event publishing
        ArgumentCaptor<WalletCreatedEvent> eventCaptor = ArgumentCaptor.forClass(WalletCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        WalletCreatedEvent event = eventCaptor.getValue();
        assertThat(event).isNotNull();
        assertThat(event.getWalletId()).isEqualTo(savedWallet.getId());
        assertThat(event.getUserId()).isEqualTo(savedWallet.getUserId());
        assertThat(event.getCurrency()).isEqualTo(savedWallet.getCurrency().name());
        assertThat(event.getCorrelationId()).isNotBlank();
    }

    @Test
    @DisplayName("Should throw DuplicateWalletException when wallet already exists for user and currency")
    void shouldThrowDuplicateWalletExceptionWhenWalletExists() {
        // given
        when(walletRepository.existsByUserIdAndCurrency(userId, CurrencyType.BRL)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> walletService.createWallet(request))
                .isInstanceOf(DuplicateWalletException.class)
                .hasMessageContaining("Wallet already exists for user");

        verify(walletRepository, never()).save(any(Wallet.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should propagate DataIntegrityViolationException if database constraint is violated")
    void shouldPropagateDatabaseException() {
        // given
        when(walletRepository.existsByUserIdAndCurrency(userId, CurrencyType.BRL)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenThrow(DataIntegrityViolationException.class);

        // when / then
        assertThatThrownBy(() -> walletService.createWallet(request))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(eventPublisher, never()).publishEvent(any());
    }
}
