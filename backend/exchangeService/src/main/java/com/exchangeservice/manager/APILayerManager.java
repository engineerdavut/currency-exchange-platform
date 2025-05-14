package com.exchangeservice.manager;

import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;


import java.math.BigDecimal;
import java.math.RoundingMode;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

@Component
@Primary
public class APILayerManager implements PriceManager {
    private final Logger logger = LoggerFactory.getLogger(APILayerManager.class);
    private final BigDecimal OUNCE_TO_GRAM = new BigDecimal("31.1034768");
    
    @Value("${api.layer.key}")
    private String apiKey;
    @Value("${api.layer.url}")
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
                HttpResponse<String> response = Unirest.get(apiUrl)
                    .header("apikey", apiKey)
                    .queryString("base", "XAU")
                    .queryString("symbols", toCurrency)
                    .asString();
                
                JSONObject json = new JSONObject(response.getBody());
                BigDecimal ounceRate = json.getJSONObject("rates").getBigDecimal(toCurrency);
                BigDecimal gramRate = ounceRate.divide(OUNCE_TO_GRAM, 6, RoundingMode.HALF_UP);
                logger.info("GOLD to {}: 1 gram = {} {}", toCurrency, gramRate, toCurrency);
                return gramRate;
            } else {
                HttpResponse<String> response = Unirest.get(apiUrl)
                    .header("apikey", apiKey)
                    .queryString("base", fromCurrency)
                    .queryString("symbols", "XAU")
                    .asString();
                
                JSONObject json = new JSONObject(response.getBody());
                BigDecimal ounceRate = json.getJSONObject("rates").getBigDecimal("XAU");
                BigDecimal gramPerCurrency = ounceRate.multiply(OUNCE_TO_GRAM);
                logger.info("{} to GOLD: 1 {} = {} gram", fromCurrency, fromCurrency, gramPerCurrency);
                return BigDecimal.ONE.divide(gramPerCurrency, 6, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            logger.error("Error handling gold exchange rate from {} to {}", fromCurrency, toCurrency, e);
            throw new RuntimeException("Failed to calculate gold exchange rate: " + e.getMessage());
        }
    }
    

    @Override
    public ExchangeRateInfo getExchangeRateInfo(String fromCurrency, String toCurrency) throws Exception {
        if (fromCurrency.equals("GOLD") || toCurrency.equals("GOLD")) {
            BigDecimal rate;
            OperationType operationType;
            
            if (fromCurrency.equals("GOLD")) {
                rate = handleGoldExchangeRate(fromCurrency, toCurrency);
                operationType = OperationType.MULTIPLY;
            } else {
                rate = handleGoldExchangeRate(fromCurrency, toCurrency);
                operationType = OperationType.DIVIDE;
            }
            
            return new ExchangeRateInfo(rate, operationType);
        }
        
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        
        if (fromCurrency.equals("USD") && toCurrency.equals("TRY") || 
            fromCurrency.equals("EUR") && toCurrency.equals("TRY")) {
            return new ExchangeRateInfo(rate, OperationType.MULTIPLY);
        }

        else if (toCurrency.equals("USD") && fromCurrency.equals("TRY") || 
                 toCurrency.equals("EUR") && fromCurrency.equals("TRY")) {
            return new ExchangeRateInfo(getExchangeRate(toCurrency, fromCurrency), OperationType.DIVIDE);
        }
        else {
            return new ExchangeRateInfo(rate, OperationType.MULTIPLY);
        }
    }
}
