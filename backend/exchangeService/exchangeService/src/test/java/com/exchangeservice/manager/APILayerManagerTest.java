package com.exchangeservice.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
        // Inject test values for properties
        ReflectionTestUtils.setField(apiLayerManager, "apiKey", testApiKey);
        ReflectionTestUtils.setField(apiLayerManager, "apiUrl", testApiUrl);
        
    }

    @Test
    void getGoldPrices_ShouldReturnBuyAndSellPrices() throws Exception {
        // ... (Arrange kısmı aynı) ...
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(eq("apikey"), eq("test-api-key"))).thenReturn(getRequest); // apiKey mock'unu spesifik yapalım
            when(getRequest.queryString("base", "XAU")).thenReturn(getRequest);
            when(getRequest.queryString("symbols", "TRY")).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":31034.768}}"
            );
    
            // Act
            BigDecimal[] prices = apiLayerManager.getGoldPrices();
    
            // Assert
            assertNotNull(prices);
            assertEquals(2, prices.length);
            // Düzeltilmiş beklenen değerler
            assertEquals(new BigDecimal("992.80"), prices[0]); // Bankanın ALIŞ fiyatı (kullanıcı satarken)
            assertEquals(new BigDecimal("1002.78"), prices[1]);// Bankanın SATIŞ fiyatı (kullanıcı alırken)
        }
    }

    @Test
    void getExchangeRate_StandardCurrencies_ShouldReturnRate() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"USD\":0.035}}"
            );

            // Act
            BigDecimal rate = apiLayerManager.getExchangeRate("TRY", "USD");

            // Assert
            assertEquals(new BigDecimal("0.035"), rate);
        }
    }

    @Test
    void getExchangeRateInfo_TRYtoUSD_ShouldReturnDivideOperation() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {

            // Mock for the first call to getExchangeRate("TRY", "USD")
            // which happens inside getExchangeRateInfo before the "else if"
            GetRequest firstCallGetRequest = mock(GetRequest.class, "firstCallGetRequest");
            HttpResponse<String> firstCallHttpResponse = mock(HttpResponse.class, "firstCallHttpResponse");

            unirestMock.when(() -> Unirest.get(eq(testApiUrl))) // apiUrl ile eşleşen ilk get
                       .thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.header(eq("apikey"), eq(testApiKey))).thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.queryString("base", "TRY")).thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.queryString("symbols", "USD")).thenReturn(firstCallGetRequest);
            when(firstCallGetRequest.asString()).thenReturn(firstCallHttpResponse);
            when(firstCallHttpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"USD\":0.0350}}" // TRY -> USD response
            );

            // Mock for the second call to getExchangeRate("USD", "TRY")
            // which happens inside the "else if" block of getExchangeRateInfo
            GetRequest secondCallGetRequest = mock(GetRequest.class, "secondCallGetRequest");
            HttpResponse<String> secondCallHttpResponse = mock(HttpResponse.class, "secondCallHttpResponse");

            // Unirest.get(testApiUrl) ikinci kez çağrıldığında farklı bir GetRequest mock'u döndürmeli.
            // Mockito, aynı metoda yapılan ardışık çağrılar için farklı davranışlar tanımlamanıza izin verir.
            unirestMock.when(() -> Unirest.get(eq(testApiUrl)))
                       .thenReturn(firstCallGetRequest) // İlk çağrı için
                       .thenReturn(secondCallGetRequest); // İkinci çağrı için

            // İkinci çağrı için GetRequest zincirini mock'la
            when(secondCallGetRequest.header(eq("apikey"), eq(testApiKey))).thenReturn(secondCallGetRequest);
            when(secondCallGetRequest.queryString("base", "USD")).thenReturn(secondCallGetRequest);
            when(secondCallGetRequest.queryString("symbols", "TRY")).thenReturn(secondCallGetRequest);
            when(secondCallGetRequest.asString()).thenReturn(secondCallHttpResponse);
            when(secondCallHttpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":28.50}}" // USD -> TRY response
            );

            // Act
            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("TRY", "USD");

            // Assert
            assertNotNull(info, "ExchangeRateInfo should not be null");
            assertEquals(OperationType.DIVIDE, info.getOperationType());
            // getExchangeRateInfo, içindeki getExchangeRate("USD", "TRY") çağrısının sonucunu kullanır.
            assertEquals(new BigDecimal("28.50"), info.getRate());

            // İki farklı Unirest.get çağrısının yapıldığını ve doğru parametrelerle yapıldığını doğrula
            // (Bu biraz daha karmaşık olabilir, çünkü aynı `apiUrl` kullanılıyor)
            // Şimdilik temel işlevselliğe odaklanalım.
            // Eğer daha detaylı verify yapmak isterseniz, ArgumentCaptor kullanabilirsiniz.
            // Örneğin, queryString("base", captor) ile base parametresinin
            // ilk çağrıda "TRY", ikinci çağrıda "USD" olduğunu doğrulayabilirsiniz.
        }
    }

    @Test
    void getExchangeRateInfo_USDtoTRY_ShouldReturnMultiplyOperation() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":28.5}}"
            );

            // Act
            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("USD", "TRY");

            // Assert
            assertEquals(new BigDecimal("28.5"), info.getRate());
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_TRYtoGOLD_ShouldReturnDivideOperation() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"XAU\":0.00001}}"
            );

            // Act
            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("TRY", "GOLD");

            // Assert
            assertNotNull(info);
            assertEquals(OperationType.DIVIDE, info.getOperationType());
        }
    }

    @Test
    void getExchangeRateInfo_GOLDtoTRY_ShouldReturnMultiplyOperation() throws Exception {
        // Arrange
        try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
            unirestMock.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
            when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.queryString(anyString(), anyString())).thenReturn(getRequest);
            when(getRequest.asString()).thenReturn(httpResponse);
            when(httpResponse.getBody()).thenReturn(
                "{\"success\":true,\"rates\":{\"TRY\":31034.768}}"
            );

            // Act
            ExchangeRateInfo info = apiLayerManager.getExchangeRateInfo("GOLD", "TRY");

            // Assert
            assertNotNull(info);
            assertEquals(OperationType.MULTIPLY, info.getOperationType());
        }
    }
}
