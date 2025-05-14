package com.exchangeservice.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kong.unirest.core.GetRequest;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

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
        ReflectionTestUtils.setField(exchangeRateAPIManager, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(exchangeRateAPIManager, "apiUrl", "https://v6.exchangerate-api.com/v6/");
    }

    @Test
    void getGoldPrices_ShouldThrowUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            exchangeRateAPIManager.getGoldPrices();
        });
    }

    @Test
    void getExchangeRate_StandardCurrencies_ShouldReturnRate() throws Exception {
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"USD\":0.035}}"
            );

            BigDecimal rate = exchangeRateAPIManager.getExchangeRate("TRY", "USD");

            assertEquals(new BigDecimal("0.035"), rate);
        }
    }

    @Test
    void getExchangeRateInfo_TRYtoUSD_ShouldReturnDivideOperation() throws Exception {
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"USD\":0.035}}"
            ).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"TRY\":28.5}}"
            );

            ExchangeRateInfo info = exchangeRateAPIManager.getExchangeRateInfo("TRY", "USD");

            assertEquals(new BigDecimal("28.5"), info.getRate());
            assertEquals(OperationType.DIVIDE, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_USDtoTRY_ShouldReturnMultiplyOperation() throws Exception {
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"TRY\":28.5}}"
            );

            ExchangeRateInfo info = exchangeRateAPIManager.getExchangeRateInfo("USD", "TRY");

            assertEquals(new BigDecimal("28.5"), info.getRate());
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_USDtoEUR_ShouldReturnMultiplyOperation() throws Exception {

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"result\":\"success\",\"conversion_rates\":{\"EUR\":0.92}}"
            );

            ExchangeRateInfo info = exchangeRateAPIManager.getExchangeRateInfo("USD", "EUR");


            assertEquals(new BigDecimal("0.92"), info.getRate());
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }
}
