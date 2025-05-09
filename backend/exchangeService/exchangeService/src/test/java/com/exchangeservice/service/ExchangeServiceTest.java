package com.exchangeservice.service;

import com.exchangeservice.config.RabbitMQConfig;
import com.exchangeservice.dto.*;
import com.exchangeservice.entity.ExchangeTransaction;
import com.exchangeservice.manager.ExchangeRateInfo;
import com.exchangeservice.manager.OperationType;
import com.exchangeservice.manager.PriceManager;
import com.exchangeservice.messaging.RabbitMQListener;
import com.exchangeservice.repository.ExchangeTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.math.RoundingMode;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) 
class ExchangeServiceTest {

    @Mock
    @Qualifier("APILayerManager")
    private PriceManager goldPriceManager;

    @Mock
    @Qualifier("exchangeRateAPIManager")
    private PriceManager currencyPriceManager;

    @Mock
    private ExchangeTransactionRepository transactionRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQListener rabbitMQListener;

    @Captor
    private ArgumentCaptor<BalanceCheckRequestDto> balanceCheckCaptor;

    @Captor
    private ArgumentCaptor<BalanceUpdateRequestDto> balanceUpdateCaptor;

    @Captor
    private ArgumentCaptor<ExchangeTransaction> transactionCaptor;

    @Mock
    @Qualifier("exchangeService")
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        // exchangeService'i manuel olarak oluşturun
        exchangeService = new ExchangeService(
            goldPriceManager,
            currencyPriceManager,
            transactionRepository,
            rabbitTemplate,
            rabbitMQListener
        );
        // Diğer genel mock tanımlamalarınız varsa buraya gelebilir,
        // ama spesifik when().thenReturn() çağrıları genellikle test metodunun içinde olmalı.
    }

    private ExchangeRequestDto createRequest(String username, String fromCurrency, String toCurrency, 
                                           BigDecimal amount, String transactionType) {
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setUsername(username);
        request.setAccountId(1L);
        request.setFromCurrency(fromCurrency);
        request.setToCurrency(toCurrency);
        request.setAmount(amount);
        request.setTransactionType(transactionType);
        return request;
    }

    @Test
    void processExchange_StandardCurrencyExchange_Success() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "USD", BigDecimal.valueOf(1000), "BUY");
        
        // Mock exchange rate info
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(28.5), OperationType.DIVIDE);
        when(currencyPriceManager.getExchangeRateInfo("TRY", "USD")).thenReturn(rateInfo);
        
        // Mock balance check response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(true);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong())).thenReturn(balanceResponse);
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser",request);
        
        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(BigDecimal.valueOf(28.5), response.getExecutedPrice());
        assertEquals(BigDecimal.valueOf(1000), response.getFromAmount());
        assertEquals("TRY", response.getFromCurrency());
        assertEquals(BigDecimal.valueOf(35.09).setScale(2, RoundingMode.HALF_UP), response.getToAmount());
        assertEquals("USD", response.getToCurrency());
        
        // Verify balance check was requested
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.BALANCE_CHECK_EXCHANGE),
            eq(RabbitMQConfig.BALANCE_CHECK_ROUTING_KEY),
            balanceCheckCaptor.capture());
        assertEquals("testUser", balanceCheckCaptor.getValue().getUsername());
        assertEquals("TRY", balanceCheckCaptor.getValue().getCurrency());
        assertEquals(BigDecimal.valueOf(1000), balanceCheckCaptor.getValue().getAmount());
        
        // Verify balance update was requested
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.BALANCE_UPDATE_EXCHANGE),
            eq(RabbitMQConfig.BALANCE_UPDATE_ROUTING_KEY),
            balanceUpdateCaptor.capture());
        assertEquals("testUser", balanceUpdateCaptor.getValue().getUsername());
        assertEquals("TRY", balanceUpdateCaptor.getValue().getFromCurrency());
        assertEquals("USD", balanceUpdateCaptor.getValue().getToCurrency());
        assertEquals(BigDecimal.valueOf(1000), balanceUpdateCaptor.getValue().getFromAmount());
        assertEquals(BigDecimal.valueOf(35.09).setScale(2, RoundingMode.HALF_UP), 
                     balanceUpdateCaptor.getValue().getToAmount());
        
        // Verify transaction was saved
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(1L, transactionCaptor.getValue().getAccountId());
        assertEquals("TRY", transactionCaptor.getValue().getFromCurrency());
        assertEquals("USD", transactionCaptor.getValue().getToCurrency());
        assertEquals(BigDecimal.valueOf(1000), transactionCaptor.getValue().getFromAmount());
        assertEquals(BigDecimal.valueOf(35.09).setScale(2, RoundingMode.HALF_UP), 
                     transactionCaptor.getValue().getToAmount());
        assertEquals("BUY", transactionCaptor.getValue().getTransactionType());
    }

    @Test
    void processExchange_GoldPurchase_Success() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "GOLD", BigDecimal.valueOf(30000), "BUY");
        
        // Mock exchange rate info for gold
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(2500), OperationType.DIVIDE);
        when(goldPriceManager.getExchangeRateInfo("TRY", "GOLD")).thenReturn(rateInfo);
        
        // Mock balance check response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(true);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong())).thenReturn(balanceResponse);
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser",request);
        
        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(BigDecimal.valueOf(2500), response.getExecutedPrice());
        
        // For gold, we expect the actual amount to be adjusted to whole grams
        // 30000 TRY / 2500 = 12 grams (exact: 12.0)
        // So actual cost is 12 * 2500 = 30000 TRY
        assertEquals(BigDecimal.valueOf(30000), response.getFromAmount());
        assertEquals("TRY", response.getFromCurrency());
        assertEquals(BigDecimal.valueOf(12), response.getToAmount());
        assertEquals("GOLD", response.getToCurrency());
        
        // Verify transaction was saved with correct amounts
        verify(transactionRepository).save(transactionCaptor.capture());
        assertEquals(BigDecimal.valueOf(30000), transactionCaptor.getValue().getFromAmount());
        assertEquals(BigDecimal.valueOf(12), transactionCaptor.getValue().getToAmount());
    }

    @Test
    void processExchange_GoldPurchaseWithFractionalAmount_RoundsDown() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "GOLD", BigDecimal.valueOf(31000), "BUY");
        
        // Mock exchange rate info for gold
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(2500), OperationType.DIVIDE);
        when(goldPriceManager.getExchangeRateInfo("TRY", "GOLD")).thenReturn(rateInfo);
        
        // Mock balance check response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(true);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong())).thenReturn(balanceResponse);
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser",request);
        
        // Assert
        assertEquals("SUCCESS", response.getStatus());
        
        // 31000 TRY / 2500 = 12.4 grams, which rounds down to 12 grams
        // So actual cost is 12 * 2500 = 30000 TRY (not the full 31000)
        assertEquals(BigDecimal.valueOf(30000), response.getFromAmount());
        assertEquals(BigDecimal.valueOf(12), response.getToAmount());
    }

    @Test
    void processExchange_InsufficientBalance_ReturnsFailure() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "USD", BigDecimal.valueOf(1000), "BUY");
        
        // Mock exchange rate info
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(28.5), OperationType.DIVIDE);
        when(currencyPriceManager.getExchangeRateInfo("TRY", "USD")).thenReturn(rateInfo);
        
        // Mock insufficient balance response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(false);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong())).thenReturn(balanceResponse);
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser",request);
        
        // Assert
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Exchange failed"));
        
        // Verify no balance update or transaction save occurred
        verify(rabbitTemplate, never()).convertAndSend(
            eq(RabbitMQConfig.BALANCE_UPDATE_EXCHANGE),
            eq(RabbitMQConfig.BALANCE_UPDATE_ROUTING_KEY),
            any(BalanceUpdateRequestDto.class));
        verify(transactionRepository, never()).save(any(ExchangeTransaction.class));
    }

    @Test
    void processExchange_BalanceCheckTimeout_ReturnsFailure() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "USD", BigDecimal.valueOf(1000), "BUY");
        
        // Mock exchange rate info
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(28.5), OperationType.DIVIDE);
        when(currencyPriceManager.getExchangeRateInfo("TRY", "USD")).thenReturn(rateInfo);
        
        // Mock timeout (null response)
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong())).thenReturn(null);
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser",request);
        
        // Assert
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Exchange failed"));
    }

    @Test
    void processExchange_ExchangeRateError_ReturnsFailure() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "INVALID", BigDecimal.valueOf(1000), "BUY");
        
        // Mock exchange rate error
        when(currencyPriceManager.getExchangeRateInfo("TRY", "INVALID"))
            .thenThrow(new RuntimeException("Invalid currency"));
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser",request);
        
        // Assert
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Exchange failed"));
    }

    @Test
    void processExchange_MultiplyOperation_CalculatesCorrectly() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "USD", "TRY", BigDecimal.valueOf(100), "SELL");
        
        // Mock exchange rate info with MULTIPLY operation
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(28.5), OperationType.MULTIPLY);
        when(currencyPriceManager.getExchangeRateInfo("USD", "TRY")).thenReturn(rateInfo);
        
        // Mock balance check response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(true);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong())).thenReturn(balanceResponse);
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser",request);
        
        // Assert
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(BigDecimal.valueOf(100), response.getFromAmount());
        assertEquals("USD", response.getFromCurrency());
        assertEquals(BigDecimal.valueOf(2850).setScale(2, RoundingMode.HALF_UP), response.getToAmount());
        assertEquals("TRY", response.getToCurrency());
    }

    @Test
    void processExchange_LessThanOneGramGold_ThrowsException() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "GOLD", BigDecimal.valueOf(1000), "BUY");
        
        // Mock exchange rate info for gold - 1000 TRY can buy only 0.4 grams
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(2500), OperationType.DIVIDE);
        when(goldPriceManager.getExchangeRateInfo("TRY", "GOLD")).thenReturn(rateInfo);
        
        // Act & Assert
        ExchangeResponseDto response = exchangeService.processExchange("testUser", request);
        
        // Verify response indicates failure due to minimum gold amount
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Minimum gold purchase"));
    }

    @Test
    void processExchange_InterruptedBalanceCheck_ReturnsFailure() throws Exception {
        // Arrange
        ExchangeRequestDto request = createRequest("testUser", "TRY", "USD", BigDecimal.valueOf(1000), "BUY");
        
        // Mock exchange rate info
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(BigDecimal.valueOf(28.5), OperationType.DIVIDE);
        when(currencyPriceManager.getExchangeRateInfo("TRY", "USD")).thenReturn(rateInfo);
        
        // Mock InterruptedException during balance check
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong()))
            .thenThrow(new InterruptedException("Test interruption"));
        
        // Act
        ExchangeResponseDto response = exchangeService.processExchange("testUser", request);
        
        // Assert
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Exchange failed"));
    }
    
}


