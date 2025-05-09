package com.exchangeservice.manager;

import com.exchangeservice.dto.ExchangeRequestDto;
import com.exchangeservice.dto.ExchangeResponseDto;
import com.exchangeservice.service.ExchangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeManagerTest {

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private ExchangeManager exchangeManager;

    @Test
    void processExchange_ShouldDelegateToExchangeService() throws Exception {
        // Arrange
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setUsername("testUser");
        request.setFromCurrency("TRY");
        request.setToCurrency("USD");
        request.setAmount(new BigDecimal("1000"));
        request.setTransactionType("BUY");

        ExchangeResponseDto expectedResponse = new ExchangeResponseDto();
        expectedResponse.setStatus("SUCCESS");
        expectedResponse.setMessage("Purchase completed");

        when(exchangeService.processExchange("testUser",request)).thenReturn(expectedResponse);

        // Act
        ExchangeResponseDto actualResponse = exchangeManager.processExchange("testUser",request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(exchangeService).processExchange("testUser",request);
    }
}

