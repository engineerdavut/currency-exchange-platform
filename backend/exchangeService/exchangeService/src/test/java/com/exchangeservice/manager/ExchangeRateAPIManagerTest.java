package com.exchangeservice.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kong.unirest.GetRequest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class ExchangeRateAPIManagerTest {

    @InjectMocks
    private ExchangeRateAPIManager exchangeRateAPIManager;

    @Mock
    private GetRequest getRequest;
    @Mock
    private HttpResponse<String> httpResponse;

    @BeforeEach
    void setUp() {
        // Inject test values for properties
        ReflectionTestUtils.setField(exchangeRateAPIManager, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(exchangeRateAPIManager, "apiUrl", "https://v6.exchangerate-api.com/v6/");
    }

    @Test
    void getGoldPrices_ShouldThrowUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            exchangeRateAPIManager.getGoldPrices();
        });
    }

    @Test
    void getExchangeRate_StandardCurrencies_ShouldReturnRate() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"USD\":0.035}}"
            );

            // Act
            BigDecimal rate = exchangeRateAPIManager.getExchangeRate("TRY", "USD");

            // Assert
            assertEquals(new BigDecimal("0.035"), rate);
        }
    }

    @Test
    void getExchangeRateInfo_TRYtoUSD_ShouldReturnDivideOperation() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            
            // First call for TRY to USD
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"USD\":0.035}}"
            ).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"TRY\":28.5}}"
            );

            // Act
            ExchangeRateInfo info = exchangeRateAPIManager.getExchangeRateInfo("TRY", "USD");

            // Assert
            assertEquals(new BigDecimal("28.5"), info.getRate());
            assertEquals(OperationType.DIVIDE, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_USDtoTRY_ShouldReturnMultiplyOperation() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"TRY\":28.5}}"
            );

            // Act
            ExchangeRateInfo info = exchangeRateAPIManager.getExchangeRateInfo("USD", "TRY");

            // Assert
            assertEquals(new BigDecimal("28.5"), info.getRate());
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_USDtoEUR_ShouldReturnMultiplyOperation() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"EUR\":0.92}}"
            );

            // Act
            ExchangeRateInfo info = exchangeRateAPIManager.getExchangeRateInfo("USD", "EUR");

            // Assert
            assertEquals(new BigDecimal("0.92"), info.getRate());
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }
}
