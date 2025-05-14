package com.exchangeservice.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

@ExtendWith(MockitoExtension.class)
class APILayerManagerTest {

    @InjectMocks
    private APILayerManager apiLayerManager;
    @Mock
    private GetRequest getRequest;
    @Mock
    private HttpResponse<String> httpResponse;
    
    private String testApiKey = "test-api-key";
    private String testApiUrl = "https://api.apilayer.com/exchangerates_data/latest";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(apiLayerManager, "apiKey", testApiKey);
        ReflectionTestUtils.setField(apiLayerManager, "apiUrl", testApiUrl);
    }

    @Test
    void getGoldPrices_ShouldReturnBuyAndSellPrices() throws Exception {
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(eq("apikey"), eq("test-api-key"))).thenReturn(getRequest); // apiKey mock'unu spesifik yapalım
            when(getRequest.queryString("base", "XAU")).thenReturn(getRequest);
            when(getRequest.queryString("symbols", "TRY")).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":31034.768}}"
            );
    
            BigDecimal[] prices = apiLayerManager.getGoldPrices();
    

            assertNotNull(prices);
            assertEquals(2, prices.length);

            assertEquals(new BigDecimal("992.80"), prices[0]);
            assertEquals(new BigDecimal("1002.78"), prices[1]);
        }
    }

    @Test
    void getExchangeRate_StandardCurrencies_ShouldReturnRate() throws Exception {
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"USD\":0.035}}"
            );

            BigDecimal rate = apiLayerManager.getExchangeRate("TRY", "USD");


            assertEquals(new BigDecimal("0.035"), rate);
        }
    }

    @Test
    void getExchangeRateInfo_TRYtoUSD_ShouldReturnDivideOperation() throws Exception {

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {

            GetRequest firstCallGetRequest = mock(GetRequest.class, "firstCallGetRequest");
            HttpResponse<String> firstCallHttpResponse = mock(HttpResponse.class, "firstCallHttpResponse");

            unirestMock.when(() -> Unirest.get(eq(testApiUrl)))
                       .thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.header(eq("apikey"), eq(testApiKey))).thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.queryString("base", "TRY")).thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.queryString("symbols", "USD")).thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.asString()).thenReturn(firstCallHttpResponse);
            when(firstCallHttpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"USD\":0.0350}}" 
            );

            GetRequest secondCallGetRequest = mock(GetRequest.class, "secondCallGetRequest");
            HttpResponse<String> secondCallHttpResponse = mock(HttpResponse.class, "secondCallHttpResponse");

            unirestMock.when(() -> Unirest.get(eq(testApiUrl)))
                       .thenReturn(firstCallGetRequest)
                       .thenReturn(secondCallGetRequest);

            when(secondCallGetRequest.header(eq("apikey"), eq(testApiKey))).thenReturn(secondCallGetRequest);
            when(secondCallGetRequest.queryString("base", "USD")).thenReturn(secondCallGetRequest);
            when(secondCallGetRequest.queryString("symbols", "TRY")).thenReturn(secondCallGetRequest);
            when(secondCallGetRequest.asString()).thenReturn(secondCallHttpResponse);
            when(secondCallHttpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":28.50}}" // USD -> TRY response
            );

            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("TRY", "USD");

            assertNotNull(info, "ExchangeRateInfo should not be null");
            assertEquals(OperationType.DIVIDE, info.getOperationType());
            assertEquals(new BigDecimal("28.50"), info.getRate());
        }
    }

    @Test
    void getExchangeRateInfo_USDtoTRY_ShouldReturnMultiplyOperation() throws Exception {
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":28.5}}"
            );

            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("USD", "TRY");

            assertEquals(new BigDecimal("28.5"), info.getRate());
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_TRYtoGOLD_ShouldReturnDivideOperation() throws Exception {

        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"XAU\":0.00001}}"
            );


            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("TRY", "GOLD");

            assertNotNull(info);
            assertEquals(OperationType.DIVIDE, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_GOLDtoTRY_ShouldReturnMultiplyOperation() throws Exception {
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":31034.768}}"
            );

            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("GOLD", "TRY");

            assertNotNull(info);
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }
}
