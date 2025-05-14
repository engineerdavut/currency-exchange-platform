package com.exchangeservice.controller;

import com.exchangeservice.dto.ExchangeRequestDto;
import com.exchangeservice.dto.ExchangeResponseDto;
import com.exchangeservice.service.ExchangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeControllerTest {

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private ExchangeController exchangeController;

    @Test
    void processExchange_SetsUsernameAndCallsService() throws Exception { 
        ExchangeRequestDto request = new ExchangeRequestDto();
        request.setFromCurrency("TRY");
        request.setToCurrency("USD");
        request.setAmount(BigDecimal.valueOf(1000));
        request.setTransactionType("BUY");
        
        String username = "testUser";
        
        ExchangeResponseDto expectedResponse = new ExchangeResponseDto();
        expectedResponse.setStatus("SUCCESS");
        expectedResponse.setMessage("Purchase completed");
        expectedResponse.setExecutedPrice(BigDecimal.valueOf(28.5));
        expectedResponse.setTimestamp(LocalDateTime.now());
        
        when(exchangeService.processExchange(anyString(), any(ExchangeRequestDto.class)))
            .thenReturn(expectedResponse);
        
        ResponseEntity<ExchangeResponseDto> responseEntity = 
            exchangeController.processExchange(username, request);
        
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertSame(expectedResponse, responseEntity.getBody());
        
        verify(exchangeService).processExchange(eq(username), argThat(req -> 
            username.equals(req.getUsername()) &&
            "TRY".equals(req.getFromCurrency()) &&
            "USD".equals(req.getToCurrency()) &&
            BigDecimal.valueOf(1000).equals(req.getAmount()) &&
            "BUY".equals(req.getTransactionType())
        ));
    }
}
