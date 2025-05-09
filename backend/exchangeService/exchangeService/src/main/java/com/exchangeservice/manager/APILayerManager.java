package com.exchangeservice.manager;

import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;


import java.math.BigDecimal;
import java.math.RoundingMode;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

@Component
@Primary
public class APILayerManager implements PriceManager {
    private final Logger logger = LoggerFactory.getLogger(APILayerManager.class);
    private final BigDecimal OUNCE_TO_GRAM = new BigDecimal("31.1034768");
    
    @Value("${EXCHANGE_API_LAYER_KEY}")
    private String apiKey;
    @Value("${EXCHANGE_API_LAYER_URL}")
    private String apiUrl;

    @Override
    public BigDecimal[] getGoldPrices() throws Exception {
        try {
            HttpResponse<String> response = Unirest.get(apiUrl)
                .header("apikey", apiKey)
                .queryString("base", "XAU")
                .queryString("symbols", "TRY")
                .asString();

            JSONObject json = new JSONObject(response.getBody());
            BigDecimal rate = json.getJSONObject("rates").getBigDecimal("TRY");
            BigDecimal gramRate = rate.divide(OUNCE_TO_GRAM, 2, RoundingMode.HALF_UP);
            
            BigDecimal buy = gramRate.multiply(new BigDecimal("0.995"));
            BigDecimal sell = gramRate.multiply(new BigDecimal("1.005"));
            
            logger.info("Fetched gold prices - Buy: {}, Sell: {}", buy, sell);
            return new BigDecimal[]{buy.setScale(2, RoundingMode.HALF_UP), sell.setScale(2, RoundingMode.HALF_UP)};
        } catch (Exception e) {
            logger.error("Error fetching gold prices from APILayer", e);
            throw new RuntimeException("Failed to fetch gold prices: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) throws Exception {
        // GOLD para birimi için özel işleme
        if (fromCurrency.equals("GOLD") || toCurrency.equals("GOLD")) {
            return handleGoldExchangeRate(fromCurrency, toCurrency);
        }
        
        try {
            HttpResponse<String> response = Unirest.get(apiUrl)
                .header("apikey", apiKey)
                .queryString("base", fromCurrency)
                .queryString("symbols", toCurrency)
                .asString();

            JSONObject json = new JSONObject(response.getBody());
            return json.getJSONObject("rates").getBigDecimal(toCurrency);
        } catch (Exception e) {
            logger.error("Error fetching exchange rate from APILayer", e);
            throw new RuntimeException("Failed to fetch exchange rate: " + e.getMessage());
        }
    }

    private BigDecimal handleGoldExchangeRate(String fromCurrency, String toCurrency) throws Exception {
        try {
            if (fromCurrency.equals("GOLD")) {
                // GOLD -> para birimi dönüşümü (altın satışı)
                HttpResponse<String> response = Unirest.get(apiUrl)
                    .header("apikey", apiKey)
                    .queryString("base", "XAU")
                    .queryString("symbols", toCurrency)
                    .asString();
                
                JSONObject json = new JSONObject(response.getBody());
                BigDecimal ounceRate = json.getJSONObject("rates").getBigDecimal(toCurrency);
                // Ons fiyatını gram fiyatına çevir (1 ons = 31.1034768 gram)
                BigDecimal gramRate = ounceRate.divide(OUNCE_TO_GRAM, 6, RoundingMode.HALF_UP);
                logger.info("GOLD to {}: 1 gram = {} {}", toCurrency, gramRate, toCurrency);
                return gramRate;
            } else {
                // Para birimi -> GOLD dönüşümü (altın alımı)
                HttpResponse<String> response = Unirest.get(apiUrl)
                    .header("apikey", apiKey)
                    .queryString("base", fromCurrency)
                    .queryString("symbols", "XAU")
                    .asString();
                
                JSONObject json = new JSONObject(response.getBody());
                BigDecimal ounceRate = json.getJSONObject("rates").getBigDecimal("XAU");
                // 1 para birimi kaç ons altın alır hesapla
                // Sonra gram cinsinden hesapla (1 ons = 31.1034768 gram)
                BigDecimal gramPerCurrency = ounceRate.multiply(OUNCE_TO_GRAM);
                logger.info("{} to GOLD: 1 {} = {} gram", fromCurrency, fromCurrency, gramPerCurrency);
                
                // 1 para birimi / 1 gram altın = kur
                return BigDecimal.ONE.divide(gramPerCurrency, 6, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            logger.error("Error handling gold exchange rate from {} to {}", fromCurrency, toCurrency, e);
            throw new RuntimeException("Failed to calculate gold exchange rate: " + e.getMessage());
        }
    }
    

    @Override
    public ExchangeRateInfo getExchangeRateInfo(String fromCurrency, String toCurrency) throws Exception {
        // GOLD para birimi için özel işleme
        if (fromCurrency.equals("GOLD") || toCurrency.equals("GOLD")) {
            BigDecimal rate;
            OperationType operationType;
            
            if (fromCurrency.equals("GOLD")) {
                // GOLD -> diğer para birimleri
                rate = handleGoldExchangeRate(fromCurrency, toCurrency);
                operationType = OperationType.MULTIPLY;
            } else {
                // Diğer para birimleri -> GOLD
                rate = handleGoldExchangeRate(fromCurrency, toCurrency);
                operationType = OperationType.DIVIDE;
            }
            
            return new ExchangeRateInfo(rate, operationType);
        }
        
        // Normal para birimleri için
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        
        // USD/TRY gibi büyük değerli kurlar için çarpma
        if (fromCurrency.equals("USD") && toCurrency.equals("TRY") || 
            fromCurrency.equals("EUR") && toCurrency.equals("TRY")) {
            return new ExchangeRateInfo(rate, OperationType.MULTIPLY);
        }
        // TRY/USD gibi küçük değerli kurlar için bölme
        else if (toCurrency.equals("USD") && fromCurrency.equals("TRY") || 
                 toCurrency.equals("EUR") && fromCurrency.equals("TRY")) {
            return new ExchangeRateInfo(getExchangeRate(toCurrency, fromCurrency), OperationType.DIVIDE);
        }
        // Diğer durumlar için
        else {
            return new ExchangeRateInfo(rate, OperationType.MULTIPLY);
        }
    }
}
