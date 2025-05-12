package com.exchangeservice.integration;

import com.exchangeservice.dto.BalanceCheckResponseDto;
import com.exchangeservice.dto.ExchangeRequestDto;
import com.exchangeservice.dto.ExchangeResponseDto;
import com.exchangeservice.entity.ExchangeTransaction;
import com.exchangeservice.manager.ExchangeRateInfo;
import com.exchangeservice.manager.OperationType;
import com.exchangeservice.manager.PriceManager;
import com.exchangeservice.messaging.RabbitMQListener;
import com.exchangeservice.repository.ExchangeTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ExchangeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExchangeTransactionRepository transactionRepository;

    @MockitoBean
    @Qualifier("APILayerManager")
    private PriceManager goldPriceManager;

    @MockitoBean
    @Qualifier("exchangeRateAPIManager")
    private PriceManager currencyPriceManager;

    @MockitoBean
    @Qualifier("collectApiPriceManager")
    private PriceManager collectApiPriceManager;

    @MockitoBean 
    private RabbitTemplate rabbitTemplate;
    
    @MockitoBean
    private RabbitMQListener rabbitMQListener;

    
    @BeforeEach
    void setUp() {
        // Test öncesi transaction repository'yi temizle
        transactionRepository.deleteAll();
    }
    
    @Test
    void processExchange_CurrencyExchange_Success() throws Exception {
        // Arrange
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setUsername("testUser");
        request.setAccountId(1L);
        request.setFromCurrency("TRY");
        request.setToCurrency("USD");
        request.setAmount(new BigDecimal("1000"));
        request.setTransactionType("BUY");
        
        // Mock exchange rate info
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(
            new BigDecimal("28.5"), OperationType.DIVIDE);
        when(currencyPriceManager.getExchangeRateInfo("TRY", "USD"))
            .thenReturn(rateInfo);
        
        // Mock balance check response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(true);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong()))
            .thenReturn(balanceResponse);
        
        // Act
        MvcResult result = mockMvc.perform(post("/api/exchange/process")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User", "testUser")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.fromCurrency").value("TRY"))
                .andExpect(jsonPath("$.toCurrency").value("USD"))
                .andReturn();
        
        // Assert
        String responseContent = result.getResponse().getContentAsString();
        ExchangeResponseDto response = objectMapper.readValue(responseContent, ExchangeResponseDto.class);
        
        assertEquals(new BigDecimal("1000"), response.getFromAmount());
        assertEquals(new BigDecimal("28.5"), response.getExecutedPrice());
        assertEquals(new BigDecimal("35.09").setScale(2, RoundingMode.HALF_UP), response.getToAmount());
        
        // Verify transaction was saved in the database
        List<ExchangeTransaction> transactions = transactionRepository.findAll();
        assertEquals(1, transactions.size());
        
        ExchangeTransaction savedTransaction = transactions.get(0);
        assertEquals("TRY", savedTransaction.getFromCurrency());
        assertEquals("USD", savedTransaction.getToCurrency());
        assertEquals(0, new BigDecimal("1000").compareTo(savedTransaction.getFromAmount()));
        assertEquals(0, new BigDecimal("35.09").setScale(2, RoundingMode.HALF_UP).compareTo(savedTransaction.getToAmount()));
        assertEquals("BUY", savedTransaction.getTransactionType());
    }
    
    @Test
    void processExchange_GoldPurchase_Success() throws Exception  {
        // Arrange
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setUsername("testUser");
        request.setAccountId(1L);
        request.setFromCurrency("TRY");
        request.setToCurrency("GOLD");
        request.setAmount(new BigDecimal("30000"));
        request.setTransactionType("BUY");
        
        // Mock exchange rate info for gold
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(
            new BigDecimal("2500"), OperationType.DIVIDE);
        when(goldPriceManager.getExchangeRateInfo("TRY", "GOLD"))
            .thenReturn(rateInfo);
        
        // Mock balance check response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(true);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong()))
            .thenReturn(balanceResponse);
        
        // Act
        MvcResult result = mockMvc.perform(post("/api/exchange/process")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User", "testUser")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.fromCurrency").value("TRY"))
                .andExpect(jsonPath("$.toCurrency").value("GOLD"))
                .andReturn();
        
        // Assert
        String responseContent = result.getResponse().getContentAsString();
        ExchangeResponseDto response = objectMapper.readValue(responseContent, ExchangeResponseDto.class);
        
        assertEquals(new BigDecimal("30000"), response.getFromAmount());
        assertEquals(new BigDecimal("2500"), response.getExecutedPrice());
        assertEquals(new BigDecimal("12"), response.getToAmount());
        
        // Verify transaction was saved in the database
        List<ExchangeTransaction> transactions = transactionRepository.findAll();
        assertEquals(1, transactions.size());
        
        ExchangeTransaction savedTransaction = transactions.get(0);
        assertEquals("TRY", savedTransaction.getFromCurrency());
        assertEquals("GOLD", savedTransaction.getToCurrency());
        assertEquals(0, new BigDecimal("30000").compareTo(savedTransaction.getFromAmount()));
        assertEquals(0, new BigDecimal("12").compareTo(savedTransaction.getToAmount()));
    }
    
    @Test
    void processExchange_InsufficientBalance_ReturnsFailed() throws Exception {
        // Arrange
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setUsername("testUser");
        request.setAccountId(1L);
        request.setFromCurrency("TRY");
        request.setToCurrency("USD");
        request.setAmount(new BigDecimal("100000")); // Büyük miktar
        request.setTransactionType("BUY");
        
        // Mock exchange rate info
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(
            new BigDecimal("28.5"), OperationType.DIVIDE);
        when(currencyPriceManager.getExchangeRateInfo("TRY", "USD"))
            .thenReturn(rateInfo);
        
        // Mock insufficient balance response
        BalanceCheckResponseDto balanceResponse = new BalanceCheckResponseDto(false);
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong()))
            .thenReturn(balanceResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/exchange/process")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User", "testUser")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Exchange failed")));
        
        // Verify no transaction was saved
        List<ExchangeTransaction> transactions = transactionRepository.findAll();
        assertEquals(0, transactions.size());
    }
    
    @Test
    void processExchange_InvalidCurrency_ReturnsFailed() throws Exception  {
        // Arrange
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setUsername("testUser");
        request.setAccountId(1L);
        request.setFromCurrency("TRY");
        request.setToCurrency("INVALID"); // Geçersiz para birimi
        request.setAmount(new BigDecimal("1000"));
        request.setTransactionType("BUY");
        
        // Mock exchange rate error
        when(currencyPriceManager.getExchangeRateInfo("TRY", "INVALID"))
            .thenThrow(new RuntimeException("Unsupported currency"));
        
        // Act & Assert
        mockMvc.perform(post("/api/exchange/process")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User", "testUser")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Exchange failed")));
        
        // Verify no transaction was saved
        List<ExchangeTransaction> transactions = transactionRepository.findAll();
        assertEquals(0, transactions.size());
    }
    
    @Test
    void processExchange_BalanceCheckTimeout_ReturnsFailed() throws Exception  {
        // Arrange
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setUsername("testUser");
        request.setAccountId(1L);
        request.setFromCurrency("TRY");
        request.setToCurrency("USD");
        request.setAmount(new BigDecimal("1000"));
        request.setTransactionType("BUY");
        
        // Mock exchange rate info
        ExchangeRateInfo rateInfo = new ExchangeRateInfo(
            new BigDecimal("28.5"), OperationType.DIVIDE);
        when(currencyPriceManager.getExchangeRateInfo("TRY", "USD"))
            .thenReturn(rateInfo);
        
        // Mock timeout (null response)
        when(rabbitMQListener.getBalanceResponse(anyString(), anyLong()))
            .thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(post("/api/exchange/process")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User", "testUser")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Exchange failed")));
        
        // Verify no transaction was saved
        List<ExchangeTransaction> transactions = transactionRepository.findAll();
        assertEquals(0, transactions.size());
    }
}
